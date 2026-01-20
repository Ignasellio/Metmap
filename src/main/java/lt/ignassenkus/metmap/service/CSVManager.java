package lt.ignassenkus.metmap.service;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CSVManager {

    /**
     * Reads a specific slice of a SINGLE row.
     * Supports negative indexing by looking from the end.
     *
     * @param filePath         Path to the CSV file
     * @param rowIndex         Row index. Negative values count from end (-1 is last row).
     * @param startColumnIndex Column start. Negative counts from end. null = 0.
     * @param endColumnIndex   Column end. Negative counts from end. null = end of line.
     */
    public static List<String> readRow(String filePath, int rowIndex, Integer startColumnIndex, Integer endColumnIndex) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setInputBufferSize(1024 * 64);
        settings.setMaxCharsPerColumn(100_000);

        CsvParser parser = new CsvParser(settings);
        List<String> results = new ArrayList<>();

        String[] targetRow = null;
        try (BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath(), StandardCharsets.UTF_8)) {
            parser.beginParsing(reader);
            String[] row;
            long currentIndex = 0;

            if (rowIndex >= 0) {
                while ((row = parser.parseNext()) != null) {
                    if (currentIndex == rowIndex) {
                        targetRow = row;
                        break;
                    }
                    currentIndex++;
                }
            } else {
                int lookBack = Math.abs(rowIndex);
                LinkedList<String[]> buffer = new LinkedList<>();

                while ((row = parser.parseNext()) != null) {
                    buffer.add(row);
                    if (buffer.size() > lookBack) {
                        buffer.removeFirst();
                    }
                }

                if (buffer.size() == lookBack) {
                    targetRow = buffer.getFirst();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV row", e);
        } finally {
            parser.stopParsing();
        }

        if (targetRow != null) {
            int len = targetRow.length;

            int s = (startColumnIndex == null) ? 0 : startColumnIndex;
            if (s < 0) s = len + s;
            int e = (endColumnIndex == null) ? len : endColumnIndex;
            if (e < 0) e = len + e;

            // Clamping to avoid crashes if indices are out of bounds
            s = Math.max(0, s);
            e = Math.min(len, e);

            if (s < e) {
                results = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(targetRow, s, e)));
            }
        }

        return results;
    }

    /**
     * Reads a specific slice of a SINGLE column.
     * Supports negative indexing for columns by looking from the end.
     *
     * @param filePath    Path to CSV
     * @param columnIndex Column to read
     * @param startRow    Start row. Negative = offset from end. null = 0.
     * @param endRow      End row. Negative = offset from end. null = end of file.
     */
    public static List<String> readColumn(String filePath, int columnIndex, Integer startRow, Integer endRow) {
        boolean hasNegativeIndices = (startRow != null && startRow < 0) || (endRow != null && endRow < 0);
        if (!hasNegativeIndices) {
            return readColumnForward(filePath, columnIndex, startRow, endRow);
        }

        // Negative Index Logic (Must read full file)
        CsvParserSettings settings = new CsvParserSettings();
        settings.selectIndexes(columnIndex);
        CsvParser parser = new CsvParser(settings);

        List<String> results = new ArrayList<>();
        LinkedList<String> buffer = new LinkedList<>();

        int s = (startRow == null) ? 0 : startRow;
        Integer e = endRow; // Keep object to know if null

        try (BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath(), StandardCharsets.UTF_8)) {
            parser.beginParsing(reader);
            String[] row;
            long currentIndex = 0;

            if (s >= 0 && e != null && e < 0) {
                int excludeLast = Math.abs(e);

                while ((row = parser.parseNext()) != null) {
                    String val = (row.length > 0) ? row[0] : null;

                    if (currentIndex >= s) {
                        buffer.add(val);
                        // If buffer exceeds the exclusion zone, the oldest item is safe to add
                        if (buffer.size() > excludeLast) {
                            results.add(buffer.removeFirst());
                        }
                    }
                    currentIndex++;
                }
            }
            else if (s < 0) {
                int keepLast = Math.abs(s);

                while ((row = parser.parseNext()) != null) {
                    String val = (row.length > 0) ? row[0] : null;
                    buffer.add(val);
                    if (buffer.size() > keepLast) {
                        buffer.removeFirst();
                    }
                }

                // If there's an end limit (e.g., start -5, end -2), slice the buffer
                if (e != null && e < 0) {
                    int cutFromEnd = Math.abs(e);
                    int limit = buffer.size() - cutFromEnd;
                    for(int i=0; i<limit; i++) results.add(buffer.get(i));
                } else {
                    results.addAll(buffer);
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Error reading CSV column from end", ex);
        } finally {
            parser.stopParsing();
        }

        return results;
    }

    // Faster method that will not work if given values referring from the last row/column
    private static List<String> readColumnForward(String filePath, int columnIndex, Integer startRow, Integer endRow) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.selectIndexes(columnIndex);

        CsvParser parser = new CsvParser(settings);
        List<String> results = new ArrayList<>();

        long start = (startRow == null) ? 0 : startRow;
        long end = (endRow == null) ? Long.MAX_VALUE : endRow;

        try (BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath(), StandardCharsets.UTF_8)) {
            parser.beginParsing(reader);
            String[] row;
            long currentRowIndex = 0;

            while ((row = parser.parseNext()) != null) {
                if (currentRowIndex >= end) break;
                if (currentRowIndex >= start) {
                    results.add((row.length > 0) ? row[0] : null);
                }
                currentRowIndex++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV column", e);
        } finally {
            parser.stopParsing();
        }
        return results;
    }

    public static String readCell(String filePath, int rowIndex, int columnIndex) {
        List<String> result = readRow(filePath, rowIndex, columnIndex, columnIndex + 1);
        if (result.isEmpty()) return null;
        return result.get(0);
    }

    /**
     * Reads a column directly into a primitive int array.
     * More memory efficient for millions of numeric rows.
     */
    public static int[] readColumnAsInt(String filePath, int columnIndex, Integer startRow, Integer endRow) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.selectIndexes(columnIndex);
        CsvParser parser = new CsvParser(settings);

        int[] data = new int[1024];
        int count = 0;

        long start = (startRow == null) ? 0 : startRow;
        long end = (endRow == null) ? Long.MAX_VALUE : endRow;

        try (BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath(), StandardCharsets.UTF_8)) {
            parser.beginParsing(reader);
            String[] row;
            long currentRowIndex = 0;

            while ((row = parser.parseNext()) != null) {
                if (currentRowIndex >= end) break;
                if (currentRowIndex >= start) {
                    // Ensuring Capacity
                    if (count == data.length) {
                        data = Arrays.copyOf(data, data.length * 2);
                    }

                    String val = (row.length > 0) ? row[0] : null;
                    data[count++] = parseChromosomeValue(val);
                }
                currentRowIndex++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV column as int[]", e);
        } finally {
            parser.stopParsing();
        }

        return Arrays.copyOf(data, count);
    }

    /**
     * Reads a column directly into a String array.
     * Faster and slightly leaner than List<String>.
     */
    public static String[] readColumnAsStringArray(String filePath, int columnIndex, Integer startRow, Integer endRow) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.selectIndexes(columnIndex);
        CsvParser parser = new CsvParser(settings);

        String[] data = new String[1024];
        int count = 0;

        long start = (startRow == null) ? 0 : startRow;
        long end = (endRow == null) ? Long.MAX_VALUE : endRow;

        try (BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath(), StandardCharsets.UTF_8)) {
            parser.beginParsing(reader);
            String[] row;
            long currentRowIndex = 0;

            while ((row = parser.parseNext()) != null) {
                if (currentRowIndex >= end) break;
                if (currentRowIndex >= start) {
                    if (count == data.length) {
                        data = Arrays.copyOf(data, data.length * 2);
                    }
                    data[count++] = (row.length > 0) ? row[0] : null;
                }
                currentRowIndex++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV column as String[]", e);
        } finally {
            parser.stopParsing();
        }

        return Arrays.copyOf(data, count);
    }

    private static int parseChromosomeValue(String s) {
        if (s == null || s.isBlank()) return 0;
        String clean = s.toUpperCase().trim();
        return switch (clean) {
            case "X" -> 23;
            case "Y" -> 24;
            case "M", "MT" -> 25;
            default -> {
                try {
                    yield (int) Double.parseDouble(clean);
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
        };
    }

}
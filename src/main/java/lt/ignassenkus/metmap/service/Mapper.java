package lt.ignassenkus.metmap.service;

import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.model.Sample;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Mapper {

    // --- SINGLETON PATTERN ---
    private static Mapper instance;
    private String SampleFolder;

    private Mapper() {}

    public static synchronized Mapper getInstance() {
        if (instance == null) {
            instance = new Mapper();
        }
        return instance;
    }

    public String getSampleFolder() {return SampleFolder;}
    public void setSampleFolder(String sampleFolder) {SampleFolder = sampleFolder;}

    // --- CORE LOGIC ---

    /**
     * Iterates through a folder and processes each file.
     * This moves data from RAM to disk one sample at a time.
     */
    public List<String> compileSamples(Metadata metadata, String sourceFolderPath) {
        File folder = new File(sourceFolderPath);
        File[] listOfFiles = folder.listFiles();
        List<String> results = new ArrayList<>();

        if (listOfFiles == null) {
            System.err.println("Source folder is empty or invalid: " + sourceFolderPath);
            return null;
        }

        System.out.println("Starting batch compilation of samples...");
        for (File file : listOfFiles) {
            if (file.isFile()) {
                // Process and save each file one by one to preserve RAM
                String savedPath = saveSample(metadata, file.getAbsolutePath());
                results.add(savedPath);
            }
        }
        System.out.println("All samples compiled and saved to: " + this.SampleFolder);

        return results;
    }

    public void buildMetadata(Metadata metadata, String indexFilePath) {

        // Read all CpGs' name, chromosome and position values from metadata file
        System.out.println("Reading CpG names from metadata file...");
        String[] metaNames = CSVManager.readColumn(metadata.getFilePath(), metadata.getNameColumnIndex(), metadata.getHeaderRowIndex()+1, null)
                .toArray(new String[0]);
        System.out.println("Reading CpG chromosome locations from metadata file...");
        int[] metaChromosomes = CSVManager.readColumn(metadata.getFilePath(), metadata.getChromosomeColumnIndex(), metadata.getHeaderRowIndex()+1, null)
                .stream().mapToInt(this::parseChromosome).toArray();
        System.out.println("Reading CpG genomic locations from metadata file...");
        int[] metaLocations = CSVManager.readColumn(metadata.getFilePath(), metadata.getLocationColumnIndex(), metadata.getHeaderRowIndex()+1, null)
                .stream().mapToInt(value -> (int) (value == null ? 0.0 : Double.parseDouble(value))).toArray();

        // Reading all CpGs' names from the index file (assuming column 0 with index contains the values)
        System.out.println("Reading CpG names from Index file...");
        List<String> indexNames = CSVManager.readColumn(indexFilePath, 0, metadata.getIndexesStartRowIndex(), null);

        // --- PART 3: MAPPING & FILTERING ---
        java.util.Map<String, Integer> metaMap = new java.util.HashMap<>(metaNames.length);
        for (int i = 0; i < metaNames.length; i++) {
            metaMap.put(metaNames[i], i);
        }

        int missingInMetadata = 0;
        java.util.List<Integer> validIndices = new java.util.ArrayList<>();
        java.util.List<Integer> targetRows = new java.util.ArrayList<>();

        for (int i = 0; i < indexNames.size(); i++) {
            String cpgName = indexNames.get(i);
            Integer metaIdx = metaMap.get(cpgName);

            if (metaIdx == null) {
                missingInMetadata++;
                continue;
            }

            int chr = metaChromosomes[metaIdx];
            int loc = metaLocations[metaIdx];

            if (chr > 0 && loc > 0) {
                validIndices.add(metaIdx);
                targetRows.add(i);
            }
        }

        System.out.println("CpGs in index but not found in metadata: " + missingInMetadata);
        System.out.println("Valid CpGs found: " + validIndices.size());

        // --- PART 4: SORTING BY GENOMIC LOCATION ---
        Integer[] sortOrder = new Integer[validIndices.size()];
        for (int i = 0; i < sortOrder.length; i++) sortOrder[i] = i;

        java.util.Arrays.sort(sortOrder, (a, b) -> {
            int idxA = validIndices.get(a);
            int idxB = validIndices.get(b);
            if (metaChromosomes[idxA] != metaChromosomes[idxB]) {
                return Integer.compare(metaChromosomes[idxA], metaChromosomes[idxB]);
            }
            return Integer.compare(metaLocations[idxA], metaLocations[idxB]);
        });

        // --- PART 5: RE-ASSIGNING TO ARRAYS ---
        String[] finalNames = new String[sortOrder.length];
        int[] finalChr = new int[sortOrder.length];
        int[] finalLoc = new int[sortOrder.length];
        int[] finalTargetRows = new int[sortOrder.length];

        for (int i = 0; i < sortOrder.length; i++) {
            int originalListIdx = sortOrder[i];
            int metaIdx = validIndices.get(originalListIdx);

            finalNames[i] = metaNames[metaIdx];
            finalChr[i] = metaChromosomes[metaIdx];
            finalLoc[i] = metaLocations[metaIdx];
            finalTargetRows[i] = targetRows.get(originalListIdx);
        }

        metadata.setNames(finalNames);
        metadata.setChromosomes(finalChr);
        metadata.setLocations(finalLoc);
        metadata.setTargetRow(finalTargetRows);

        System.out.println("Metadata build complete. Sorted by location.");
    }

    /**
     * Loads a processed sample file from the designated SampleFolder.
     * * @param fileName The name of the file (e.g., "sample1.csv") to load.
     * @return A Sample object populated with the file path and methylation data.
     */
    public Sample loadSample(String fileName) {
        // 1. Construct the full path using the designated SampleFolder
        File file = new File(fileName);
        String fullPath = file.getAbsolutePath();

        if (!file.exists()) {
            System.err.println("Sample file not found: " + fullPath);
            return null;
        }

        System.out.println("Loading sample: " + fullPath);

        // 2. Read the methylation values from the first column (index 0)
        List<String> rawValues = CSVManager.readColumn(fullPath, 0, 0, null);

        // 3. Convert the String list to a float array for the Sample model
        float[] methylations = new float[rawValues.size()];
        for (int i = 0; i < rawValues.size(); i++) {
            String val = rawValues.get(i);

            // Consistent parsing logic: handle nulls, empties, and "NaN"
            if (val == null || val.isBlank() || val.equalsIgnoreCase("NaN") || val.equals("\"\"")) {
                methylations[i] = Float.NaN;
            } else {
                try {
                    methylations[i] = Float.parseFloat(val);
                } catch (NumberFormatException e) {
                    methylations[i] = Float.NaN;
                }
            }
        }

        // 4. Create and populate the Sample object
        Sample sample = new Sample();
        sample.setFilePath(fullPath);
        sample.setMethylations(methylations);

        return sample;
    }

    public String saveSample(Metadata metadata, String inputPath) {
        File inputFile = new File(inputPath);
        String outputPath = SampleFolder + File.separator + inputFile.getName();

        // 1. Read the raw data into a temporary float array
        Sample sample = orderSample(metadata, inputPath);
        float[] data = sample.getMethylations();

        // 2. Write directly to a new CSV
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath)))) {
            for (int i = 0; i < data.length; i++) {
                writer.println(data[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputPath;
    }

    public Sample orderSample(Metadata metadata, String sampleFilePath) {
        System.out.println("Processing sample: " + sampleFilePath);
        List<String> rawValues = CSVManager.readColumn(
                sampleFilePath,
                0,
                metadata.getIndexesStartRowIndex(),
                null
        );
        Sample sample = new Sample();
        int[] targetRows = metadata.getTargetRow();
        float[] orderedMethylation = new float[targetRows.length];

        for (int i = 0; i < targetRows.length; i++) {
            int originalIdx = targetRows[i];

            if (originalIdx < rawValues.size()) {
                String val = rawValues.get(originalIdx);

                if (val == null || val.isEmpty() || val.equals("\"\"")) {
                    orderedMethylation[i] = Float.NaN;
                } else {
                    try {
                        orderedMethylation[i] = Float.parseFloat(val);
                    } catch (NumberFormatException e) {
                        orderedMethylation[i] = Float.NaN;
                    }
                }
            } else {
                orderedMethylation[i] = Float.NaN;
            }
        }

        sample.setMethylations(orderedMethylation);
        rawValues.clear();
        return sample;
    }

    private int parseChromosome(String s) {
        if (s == null) return 0;
        String clean = s.toUpperCase().trim().replace("CHR", "");
        return switch (clean) {
            case "X" -> 23;
            case "Y" -> 24;
            case "M", "MT" -> 25;
            default -> {
                try {
                    yield (int) Double.parseDouble(clean);
                } catch (Exception e) {
                    yield 0;
                }
            }
        };
    }
}
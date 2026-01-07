package lt.ignassenkus.metmap.service;

import lt.ignassenkus.metmap.model.Metadata;

import java.util.List;

public class Mapper {

    public static void buildMetadata(Metadata metadata, String indexFilePath) {

        // --- PART 1: ALREADY PROVIDED ---
        System.out.println("Reading CpG names from Metadata...");
        String[] metaNames = CSVManager.readColumn(metadata.getFilePath(), metadata.getNameColumnIndex(), metadata.getHeaderRowIndex()+1, null).toArray(new String[0]);

        System.out.println("Reading CpG chromosome locations...");
        int[] metaChromosomes = CSVManager.readColumn(metadata.getFilePath(), metadata.getChromosomeColumnIndex(), metadata.getHeaderRowIndex()+1, null)
                .stream().mapToInt(Mapper::parseChromosome).toArray();

        System.out.println("Reading CpG genetic locations...");
        int[] metaLocations = CSVManager.readColumn(metadata.getFilePath(), metadata.getLocationColumnIndex(), metadata.getHeaderRowIndex()+1, null)
                .stream().mapToInt(value -> (int) (value == null ? 0.0 : Double.parseDouble(value))).toArray();

        // --- PART 2: READ INDEX FILE ---
        System.out.println("Reading CpG names from Index file...");
        // Assuming column 0 in index file contains the CpG names
        List<String> indexNames = CSVManager.readColumn(indexFilePath, 0, metadata.getIndexesStartRowIndex(), null);

        // --- PART 3: MAPPING & FILTERING ---
        // Map metadata names to their array index for O(1) lookup
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

            // Check for valid location: -1, 0, or null (parsed as 0)
            int chr = metaChromosomes[metaIdx];
            int loc = metaLocations[metaIdx];

            if (chr > 0 && loc > 0) {
                validIndices.add(metaIdx);
                targetRows.add(i); // This is the row index in the sample file
            }
        }

        System.out.println("CpGs in index but not found in metadata: " + missingInMetadata);
        System.out.println("Valid CpGs found: " + validIndices.size());

        // --- PART 4: SORTING BY GENOMIC LOCATION ---
        // To save RAM, we sort an array of "pointers" to the indices
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

        // --- PART 5: RE-ASSIGNING TO ARRAYS (Final RAM cleanup) ---
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

        // Set final data back to metadata object
        metadata.setNames(finalNames);
        metadata.setChromosomes(finalChr);
        metadata.setLocations(finalLoc);
        metadata.setTargetRow(finalTargetRows);

        System.out.println("Metadata build complete. Sorted by location.");
    }

    public static void orderSamples(Metadata metadata, String FolderPath) {

    }

    // --- HELPER FUNCTIONS ---

    public static void orderSample(Metadata metadata, String SampleFilePath) {

    }

    private static int parseChromosome(String s) {
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

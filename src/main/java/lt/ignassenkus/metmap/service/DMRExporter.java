package lt.ignassenkus.metmap.service;

import lt.ignassenkus.metmap.model.DMR;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class DMRExporter {

    /**
     * Converts numeric chromosome codes back to standard names.
     */
    public static String formatChromosome(byte chr) {
        return switch (chr) {
            case 23 -> "X";
            case 24 -> "Y";
            case 25 -> "MT";
            default -> String.valueOf(chr);
        };
    }

    /**
     * Exports a list of DMRs to a CSV file.
     */
    public static void exportToCSV(List<DMR> dmrs, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            // Write Header
            writer.println("Chromosome,Start,End,MeanMethylation");

            // Write Data
            for (DMR dmr : dmrs) {
                writer.printf("%s,%d,%d,%.4f%n",
                        formatChromosome(dmr.getChromosome()),
                        dmr.getStartLocation(),
                        dmr.getEndLocation(),
                        dmr.getMeanMethylation()
                );
            }
        }
    }
}
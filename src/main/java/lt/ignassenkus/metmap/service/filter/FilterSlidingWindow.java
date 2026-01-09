package lt.ignassenkus.metmap.service.filter;

import lt.ignassenkus.metmap.model.DMR;
import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.model.Metmap;
import lt.ignassenkus.metmap.model.Sample;
import lt.ignassenkus.metmap.service.Mapper;
import java.util.ArrayList;
import java.util.List;

public class FilterSlidingWindow extends Filter {
    private final int windowSizeBp;
    private final int minValidSites; // From Python: MIN_VALID_SITES_IN_WINDOW
    private final float highLimit;   // From Python: HIGH_LIMIT
    private final float propThreshold; // From Python: PROPORTION_HIGH_SITES_THRESHOLD

    public FilterSlidingWindow(String name, int windowSizeBp, int minValidSites, float highLimit, float propThreshold) {
        super(name);
        this.windowSizeBp = windowSizeBp;
        this.minValidSites = minValidSites;
        this.highLimit = highLimit;
        this.propThreshold = propThreshold;
    }

    @Override
    public Metmap process(Metadata metadata, List<String> sampleNames) {

        System.out.println("Processing " + sampleNames.size() + " samples with filter " + this.getName());

        int[] chromosomes = metadata.getChromosomes();
        int[] locations = metadata.getLocations();
        int totalCpGs = chromosomes.length;
        int numSamples = sampleNames.size();

        // Counting how many samples have high methylation at each specific site
        short[] highIntensityCounts = new short[totalCpGs];
        for (String fileName : sampleNames) {
            Sample s = Mapper.getInstance().loadSample(fileName);
            float[] meth = s.getMethylations();

            for (int i = 0; i < totalCpGs; i++) {
                if (!Float.isNaN(meth[i]) && meth[i] >= highLimit) {
                    highIntensityCounts[i]++;
                }
            }
        }

        List<DMR> discoveredDMRs = new ArrayList<>();

        // Sliding window: Moving site-by-site
        for (int i = 0; i < totalCpGs; i++) {
            int currentChr = chromosomes[i];
            int windowStartBp = locations[i];
            int windowEndBp = windowStartBp + windowSizeBp;

            int sitesInWindow = 0;
            int highConsensusSites = 0;
            int searchIdx = i;

            // Expand window until we hit the BP limit or a new chromosome
            while (searchIdx < totalCpGs && chromosomes[searchIdx] == currentChr && locations[searchIdx] < windowEndBp) {
                sitesInWindow++;

                // A site is "Consistently High" if it meets the threshold in
                // a majority of samples (e.g., > 50%).
                if ((float) highIntensityCounts[searchIdx] / numSamples >= 0.5f) {
                    highConsensusSites++;
                }
                searchIdx++;
            }

            // Apply Python-style rigorous filters
            if (sitesInWindow >= minValidSites) {
                float proportion = (float) highConsensusSites / sitesInWindow;

                if (proportion >= propThreshold) {
                    // Calculate a representative value for this DMR
                    discoveredDMRs.add(new DMR((byte)currentChr, windowStartBp, locations[searchIdx-1], proportion));

                    // Optional: Skip to the end of this window to avoid redundant overlapping DMRs
                    i = searchIdx - 1;
                }
            }
        }

        Metmap result = new Metmap();
        result.setDMRs(discoveredDMRs.toArray(new DMR[0]));
        return result;
    }
}
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
    private final int minSites;
    private final float highMethVal;
    private final float propSitePercentThreshold;

    public FilterSlidingWindow(String name, int windowSizeBp, int minSites, float highMethVal, float propSitePercentThreshold) {
        super(name);
        this.windowSizeBp = windowSizeBp;
        this.minSites = minSites;
        this.highMethVal = highMethVal;
        this.propSitePercentThreshold = propSitePercentThreshold;
    }

    @Override
    public Metmap process(Metadata metadata, List<String> sampleNames) {
        int[] chromosomes = metadata.getChromosomes();
        int[] locations = metadata.getLocations();
        int totalCpGs = chromosomes.length;
        int numSamples = sampleNames.size();

        // 1. ACCUMULATION PHASE: Calculate global average per site
        // This allows us to read each file exactly once.
        float[] sumMethylation = new float[totalCpGs];
        int[] countValidSamples = new int[totalCpGs];

        for (String fileName : sampleNames) {
            Sample s = Mapper.getInstance().loadSample(fileName);
            float[] meth = s.getMethylations();
            for (int i = 0; i < totalCpGs; i++) {
                if (!Float.isNaN(meth[i])) {
                    sumMethylation[i] += meth[i];
                    countValidSamples[i]++;
                }
            }
        }

        // Convert sums to averages per site
        float[] siteAverages = new float[totalCpGs];
        for (int i = 0; i < totalCpGs; i++) {
            siteAverages[i] = countValidSamples[i] > 0 ? (sumMethylation[i] / countValidSamples[i]) : 0;
        }

        // 2. SLIDING WINDOW PHASE
        List<DMR> discoveredDMRs = new ArrayList<>();

        for (int i = 0; i < totalCpGs; i++) {
            int currentChr = chromosomes[i];
            int windowStartBp = locations[i];
            int windowEndBp = windowStartBp + windowSizeBp;

            int sitesInWindow = 0;
            int highMethSites = 0;
            float windowSum = 0;
            int searchIdx = i;

            // Look ahead to fill the window
            while (searchIdx < totalCpGs && chromosomes[searchIdx] == currentChr && locations[searchIdx] < windowEndBp) {
                sitesInWindow++;
                windowSum += siteAverages[searchIdx];

                // Is this specific site (averaged across samples) high?
                if (siteAverages[searchIdx] >= highMethVal) {
                    highMethSites++;
                }
                searchIdx++;
            }

            // Apply filters
            if (sitesInWindow >= minSites) {
                float proportionOfHighSites = (float) highMethSites / sitesInWindow;

                if (proportionOfHighSites >= propSitePercentThreshold) {
                    float avgWindowMeth = windowSum / sitesInWindow;

                    discoveredDMRs.add(new DMR(
                            (byte)currentChr,
                            windowStartBp,
                            locations[searchIdx - 1],
                            avgWindowMeth // The actual average methylation of the region
                    ));

                    // IMPROVEMENT: Instead of jumping to the end,
                    // we move i by 1 to truly "slide" the window.
                    // To avoid thousands of overlapping small DMRs,
                    // you can implement a "merge" logic later.
                }
            }
        }

        Metmap result = new Metmap();
        result.setDMRs(discoveredDMRs.toArray(new DMR[0]));
        return result;
    }
}
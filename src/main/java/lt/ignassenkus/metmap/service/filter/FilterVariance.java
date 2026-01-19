package lt.ignassenkus.metmap.service.filter;

import lt.ignassenkus.metmap.model.DMR;
import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.model.Metmap;
import lt.ignassenkus.metmap.model.Sample;
import lt.ignassenkus.metmap.service.Mapper;
import java.util.ArrayList;
import java.util.List;

public class FilterVariance extends Filter {
    private final int windowSizeBp;
    private final int minSites;
    private final float minVarianceThreshold;

    public FilterVariance(String name, int windowSizeBp, int minSites, float minVarianceThreshold) {
        super(name);
        this.windowSizeBp = windowSizeBp;
        this.minSites = minSites;
        this.minVarianceThreshold = minVarianceThreshold;
    }

    @Override
    public Metmap process(Metadata metadata, List<String> sampleNames) {
        int[] chromosomes = metadata.getChromosomes();
        int[] locations = metadata.getLocations();
        int totalCpGs = chromosomes.length;

        double[] aggregateMean = new double[totalCpGs];
        double[] aggregateM2 = new double[totalCpGs];
        int[] counts = new int[totalCpGs];

        for (String fileName : sampleNames) {
            Sample s = Mapper.getInstance().loadSample(fileName);
            float[] meth = s.getMethylations();

            for (int i = 0; i < totalCpGs; i++) {
                float val = meth[i];
                if (!Float.isNaN(val)) {
                    counts[i]++;
                    double delta = val - aggregateMean[i];
                    aggregateMean[i] += delta / counts[i];
                    double delta2 = val - aggregateMean[i];
                    aggregateM2[i] += delta * delta2;
                }
            }
        }

        float[] siteVariances = new float[totalCpGs];
        for (int i = 0; i < totalCpGs; i++) {
            if (counts[i] > 1) {
                siteVariances[i] = (float) (aggregateM2[i] / (counts[i] - 1));
            } else {
                siteVariances[i] = 0;
            }
        }

        List<DMR> discoveredVMRs = new ArrayList<>();

        for (int i = 0; i < totalCpGs; i++) {
            int currentChr = chromosomes[i];
            int windowStartBp = locations[i];
            int windowEndBp = windowStartBp + windowSizeBp;

            int sitesInWindow = 0;
            float windowVarSum = 0;
            int searchIdx = i;

            while (searchIdx < totalCpGs && chromosomes[searchIdx] == currentChr && locations[searchIdx] < windowEndBp) {
                sitesInWindow++;
                windowVarSum += siteVariances[searchIdx];
                searchIdx++;
            }

            if (sitesInWindow >= minSites) {
                float avgWindowVariance = windowVarSum / sitesInWindow;

                if (avgWindowVariance >= minVarianceThreshold) {
                    discoveredVMRs.add(new DMR(
                            (byte)currentChr,
                            windowStartBp,
                            locations[searchIdx - 1],
                            (float) aggregateMean[i]
                    ));
                }
            }
        }

        Metmap result = new Metmap();
        result.setDMRs(discoveredVMRs.toArray(new DMR[0]));
        return result;
    }
}

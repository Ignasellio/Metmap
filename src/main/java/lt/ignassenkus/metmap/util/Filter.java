package lt.ignassenkus.metmap.util;

import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.model.MethNames;
import lt.ignassenkus.metmap.model.Metmap;
import lt.ignassenkus.metmap.model.Sample;

import java.util.*;

public class Filter {

    Metadata metadata;
    MethNames methNames;
    private List<Sample> sampleFolder = new ArrayList<Sample>();

    private void prepareFiles(Metadata metadata, MethNames methNames, List<Sample> sampleFolder) {
        int n = metadata.getNames().length;

        // 1. Sukuriame "Vardas -> Pradinis Indeksas" žemėlapį iš MethNames.
        // Tai pasakys mums, kurioje Sample.methylations masyvo vietoje
        // yra konkretus taškas (pvz., "cg25324105" yra 5-as elementas).
        Map<String, Integer> sampleOrderMap = new HashMap<>(n);
        String[] originalOrder = methNames.getMethNames();
        for (int i = 0; i < originalOrder.length; i++) {
            sampleOrderMap.put(originalOrder[i], i);
        }

        // 2. Sukuriame indeksų masyvą [0, 1, 2, ..., n-1].
        // Rūšiuosime ne pačius duomenis, o jų pozicijas, kad išlaikytume ryšį tarp masyvų.
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;

        // 3. RŪŠIAVIMAS (atitinka Python sort_values(['chromosome', 'location']))
        // Pirmiausia lyginame chromosomas
        // Jei chromosomos vienodos, lyginame lokacijas
        Arrays.sort(indices, Comparator.comparingInt((Integer a) -> metadata.getChromosomes()[a]).thenComparingInt(a -> metadata.getLocations()[a]));

        // 4. PERRIKIUOJAME METADATA
        String[] sortedNames = new String[n];
        byte[] sortedChromosomes = new byte[n];
        int[] sortedLocations = new int[n];

        for (int i = 0; i < n; i++) {
            int oldIdx = indices[i];
            sortedNames[i] = metadata.getNames()[oldIdx];
            sortedChromosomes[i] = metadata.getChromosomes()[oldIdx];
            sortedLocations[i] = metadata.getLocations()[oldIdx];
        }

        // Atnaujiname Metadata objektą naujais surūšiuotais masyvais
        metadata.setNames(sortedNames);
        metadata.setChromosomes(sortedChromosomes);
        metadata.setLocations(sortedLocations);

        // 5. PERRIKIUOJAME SAMPLES (Suderiname su nauja Metadata tvarka)
        // Kiekvienam mėginiui sukuriame naują float[] masyvą, kur vertės sudėtos
        // pagal naująją (surūšiuotą) vardų seką.
        for (Sample sample : sampleFolder) {
            float[] oldValues = sample.getMethylations();
            float[] alignedValues = new float[n];

            for (int i = 0; i < n; i++) {
                String currentName = sortedNames[i];
                // Surandame, kur šis taškas buvo originaliame mėginio masyve
                Integer originalPos = sampleOrderMap.get(currentName);

                if (originalPos != null) {
                    alignedValues[i] = oldValues[originalPos];
                } else {
                    alignedValues[i] = Float.NaN; // Jei duomenyse trūksta taško
                }
            }
            sample.setMethylations(alignedValues);
        }

        // Galiausiai atnaujiname methNames, kad jis atspindėtų dabartinę būseną
        methNames.setMethNames(sortedNames);
    }

    public Metmap slidingWindow(Metadata metadata,
                                MethNames methNames){
        prepareFiles(metadata, methNames, sampleFolder);

        return null;
    }

}

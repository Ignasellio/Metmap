package lt.ignassenkus.metmap.model;

public class Metmap {

    private String filePath;
    public String getFilePath() {return filePath;}
    public void setFilePath(String filePath) {this.filePath = filePath;}

    private int[] originalIndexes;
    public int[] getOriginalIndexes() {return originalIndexes;}
    public void setOriginalIndexes(int[] originalIndexes) {this.originalIndexes = originalIndexes;}

    private float[] meanMethylations;
    public float[] getMeanMethylations() {return meanMethylations;}
    public void setMeanMethylations(float[] meanMethylations) {this.meanMethylations = meanMethylations;}

    private String[] names;
    public String[] getNames() {return names;}
    public void setNames(String[] name) {this.names = name;}

    private int[] chromosomes;
    public int[] getChromosomes() {return chromosomes;}
    public void setChromosomes(int[] chromosomes) {this.chromosomes = chromosomes;}

    private int[] locations;
    public int[] getLocations() {return locations;}
    public void setLocations(int[] locations) {this.locations = locations;}

    private DMR[] DMRs;
    public DMR[] getDMRs() {return DMRs;}
    public void setDMRs(DMR[] DMRs) {this.DMRs = DMRs;}

}

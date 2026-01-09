package lt.ignassenkus.metmap.model;

public class DMR {
    protected byte chromosome;
    protected int startLocation;
    protected int endLocation;
    protected float meanMethylation;

    public DMR(byte chromosome, int start, int end, float mean) {
        this.chromosome = chromosome;
        this.startLocation = start;
        this.endLocation = end;
        this.meanMethylation = mean;
    }

    // --- SETTERS & GETTERS ---
    public byte getChromosome() {return chromosome;}
    public void setChromosome(byte chromosome) {this.chromosome = chromosome;}
    public int getStartLocation() {return startLocation;}
    public void setStartLocation(int startLocation) {this.startLocation = startLocation;}
    public int getEndLocation() {return endLocation;}
    public void setEndLocation(int endLocation) {this.endLocation = endLocation;}
    public float getMeanMethylation() {return meanMethylation;}
    public void setMeanMethylation(float meanMethylation) {this.meanMethylation = meanMethylation;}

    // --- HELPERS ---
    public String toString(){
        return "chr: " + chromosome + " start: " + startLocation + " end: " + endLocation + " mean meth.: " + meanMethylation;
    }

}

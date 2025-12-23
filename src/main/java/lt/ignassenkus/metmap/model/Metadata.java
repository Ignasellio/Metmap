package lt.ignassenkus.metmap.model;

public class Metadata {
    // PARAMETERS: Actual data
    private String filePath;
    public void setFilePath(String filePath) {this.filePath = filePath;}
    public String getFilePath() {return filePath;}
    private String[] names;
    public void setNames(String[] names) {this.names = names;}
    public String[] getNames() {return names;}
    private byte[] chromosomes;
    public void setChromosomes(byte[] chromosomes) {this.chromosomes = chromosomes;}
    public byte[] getChromosomes() {return chromosomes;}
    private int[] locations;
    public void setLocations(int[] locations) {this.locations = locations;}
    public int[] getLocations() {return locations;}
    // PARAMETERS: Settings
    private Integer headerRowIndex;
    public Integer getHeaderRowIndex() {return headerRowIndex;}
    public void setHeaderRowIndex(int headerRowIndex) {this.headerRowIndex = headerRowIndex;}
    private Integer nameColumnIndex;
    public Integer getNameColumnIndex() {return nameColumnIndex;}
    public void setNameColumnIndex(int nameColumnIndex) {this.nameColumnIndex = nameColumnIndex;}
    private Integer chromosomeColumnIndex;
    public Integer getChromosomeColumnIndex() {return chromosomeColumnIndex;}
    public void setChromosomeColumnIndex(int chromosomeColumnIndex) {this.chromosomeColumnIndex = chromosomeColumnIndex;}
    private Integer locationColumnIndex;
    public Integer getLocationColumnIndex() {return locationColumnIndex;}
    public void setLocationColumnIndex(int locationColumnIndex) {this.locationColumnIndex = locationColumnIndex;}


}

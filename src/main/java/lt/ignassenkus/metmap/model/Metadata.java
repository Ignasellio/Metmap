package lt.ignassenkus.metmap.model;

import java.util.ArrayList;
import java.util.List;

public class Metadata {

    // --- VARIABLES /W SETTERS & GETTERS

    // Actual data
    private String[] names;
    public void setNames(String[] names) {this.names = names;}
    public String[] getNames() {return names;}
    private int[] chromosomes;
    public void setChromosomes(int[] chromosomes) {this.chromosomes = chromosomes;}
    public int[] getChromosomes() {return chromosomes;}
    private int[] locations;
    public void setLocations(int[] locations) {this.locations = locations;}
    public int[] getLocations() {return locations;}
    private int[] targetRows; //original row index (start count from 0) of CpG in sample (from index file)
    public void setTargetRow(int[] targetRow) {this.targetRows = targetRow;}
    public int[] getTargetRow() {return targetRows;}

    // File paths
    private String filePath;
    public void setFilePath(String filePath) {this.filePath = filePath;}
    public String getFilePath() {return filePath;}
    private String indexFilePath;
    public String getIndexFilePath() { return indexFilePath; }
    public void setIndexFilePath(String indexFilePath) { this.indexFilePath = indexFilePath; }
    private List<String> sampleFilePaths = new ArrayList<>(); // Array of already sorted samples to use
    public List<String> getSampleFilePaths() {return sampleFilePaths;}
    public void setSampleFilePaths(List<String> sampleFilePaths) {this.sampleFilePaths = sampleFilePaths;}

    // Settings to read data correctly
    private Integer IndexesStartRowIndex; // the index of first line to read in indexes file
    public Integer getIndexesStartRowIndex() {return IndexesStartRowIndex;}
    public void setIndexesStartRowIndex(Integer indexesStartRowIndex) {this.IndexesStartRowIndex = indexesStartRowIndex;}
    private Integer headerRowIndex; // the index of header row in metadata file
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

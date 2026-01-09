package lt.ignassenkus.metmap.model;

public class Sample {

    private String filePath;
    public String getFilePath() {return filePath;}
    public void setFilePath(String filePath) {this.filePath = filePath;}

    protected float[] methylations;
    public float[] getMethylations() {return methylations;}
    public void setMethylations(float[] methylations) {this.methylations = methylations;}

}

package lt.ignassenkus.metmap.service;

public class Settings {

    // --- SINGLETON PATTERN REQUIRMENTS ---

    private static Settings instance;

    private Settings() {
        toVisualize = false;
        toEmptyFolder = false;
    }

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    // --- SETTINGS (VARIABLE, SETTER, FUNCTION) ---

    // if true, will perform computer resource intensive visualizations when mapping or comparing
    // like heatmap of average methylations to show meth map etc.
    static boolean toVisualize;
    public static boolean toVisualize(){return toVisualize;}
    public void setToVisualize(boolean toVisualize) {Settings.toVisualize = toVisualize;}

    // if true, on exiting application will delete contents of folder
    // that held contents of processed samples.
    static boolean toEmptyFolder;
    public static boolean toEmptyFolder(){return toEmptyFolder;}
    public void setToEmptyFolder(boolean toEmptyFolder) {Settings.toEmptyFolder = toEmptyFolder;}

}

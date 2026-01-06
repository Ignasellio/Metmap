package lt.ignassenkus.metmap.service.filter;

import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.model.Metmap;
import lt.ignassenkus.metmap.model.Sample;

public abstract class Filter {

    // --- VARIABLES /W SETTERS & GETTERS

    // name written by user when creating/editing filters before creating a map
    protected String name;
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    // --- CONSTRUCTORS ---

    public Filter(String name) {
        init();
        this.name = name;
    }

    // --- FUNCTIONS ---

    /**
     * Initializes the filter with standard pre-determined parameter values.
     */
    public abstract void init();

    /**
     * Performs the main filter purpose. Searches for DMRs.
     * @return a Metmap object filled with discovered DMRs.
     */
    public abstract Metmap process(Metadata metadata, Sample[] samples);
}

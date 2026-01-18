package lt.ignassenkus.metmap.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lt.ignassenkus.metmap.controller.popup.filter.FilterSlidingWindowController;
import lt.ignassenkus.metmap.controller.popup.property.PropertiesIndexController;
import lt.ignassenkus.metmap.controller.popup.property.PropertiesMetadataController;
import lt.ignassenkus.metmap.model.DMR;
import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.model.Metmap;
import lt.ignassenkus.metmap.model.Sample;
import lt.ignassenkus.metmap.service.DMRExporter;
import lt.ignassenkus.metmap.service.Mapper;
import lt.ignassenkus.metmap.service.Navigation;
import lt.ignassenkus.metmap.service.filter.Filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MappingController {

    // --- OBJECTS AND HELPER FUNCTIONS ---

    private Metadata metadata;
    private String originalSampleFolder;
    private String processedSampleFolder;
    private List<Sample> Samples = new ArrayList<Sample>();
    private Metmap metmap = new Metmap();
    private ObservableList<Filter> activeFilters = FXCollections.observableArrayList();
    private ObservableList<DMR> discoveredDMRs = FXCollections.observableArrayList();
    private long[] chromosomeOffsets = new long[26]; // ends of chromosomes for visualization

    @FXML private ListView<Filter> filterListView;
    @FXML private ListView<DMR> locationListView;
    @FXML Label statusLabel;
    @FXML Label locationCountLabel;
    @FXML TextField metadataPathField;
    @FXML TextField indexPathField;
    @FXML TextField samplesPathField;
    @FXML TextField processedSamplesPathField;
    @FXML private Canvas methylationCanvas;
    @FXML private ScrollPane mapScrollPane;
    @FXML private HBox canvasContainer;

    @FXML protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML protected void onBackButtonClick(){Navigation.gotoScene("menu.fxml");}

    // --- HELPER FUNCTIONS ---

    private File getCSVFile(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);
        return fileChooser.showOpenDialog(Navigation.getStage());
    }

    // --- MAIN FUNCTIONS ---

    @FXML
    public void initialize() {
        // Setup filter list
        filterListView.setItems(activeFilters);
        filterListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Filter item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(item.getName()); }
            }
        });
        // Setup the DMR list
        locationListView.setItems(discoveredDMRs);
        locationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DMR item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Overhaul: Format how the DMR looks in the sidebar
                    // Assuming DMR has getChromosome(), getStart(), and getEnd()
                    setText(String.format("Chr: %s | Pos: %d - %d",
                            item.getChromosomeName(), item.getStartLocation(), item.getEndLocation()));
                }
            }
        });
        locationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double scale = calculateGenomeScale(metadata, methylationCanvas.getWidth());
                double targetX = getGlobalPosition(newVal.getChromosome(), newVal.getStartLocation()) * scale;

                // Calculate the scroll percentage (0.0 to 1.0)
                double hValue = targetX / methylationCanvas.getWidth();
                mapScrollPane.setHvalue(hValue);
            }
        });
    }

    // Browse Buttons
    @FXML protected void onButtonBrowseMetadataClick(){
        metadata = new Metadata();
        File chosenFile = getCSVFile();
        if(chosenFile != null){
            metadataPathField.setText(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseIndexPathClick(){
        File chosenFile = getCSVFile();
        if(chosenFile != null){
            indexPathField.setText(chosenFile.getAbsolutePath());
            metadata.setIndexFilePath(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseSamplesPathClick(){
        DirectoryChooser folderChooser = new DirectoryChooser();
        File chosenFolder = folderChooser.showDialog(Navigation.getStage());
        if(chosenFolder != null){
            samplesPathField.setText(chosenFolder.getAbsolutePath());
            //Mapper.getInstance().setSampleFolder(samplesPathField.getText());
            originalSampleFolder = samplesPathField.getText();
        }
    }
    @FXML protected void onButtonBrowseProcessedSamplesPathClick(){
        DirectoryChooser folderChooser = new DirectoryChooser();
        File chosenFolder = folderChooser.showDialog(Navigation.getStage());
        if(chosenFolder != null){
            processedSamplesPathField.setText(chosenFolder.getAbsolutePath());
            //Mapper.getInstance().setProcessedSampleFolder(processedSamplesPathField.getText());
            processedSampleFolder = processedSamplesPathField.getText();
        }
    }

    // Properties Buttons
    @FXML protected void onMetadataPropertiesButtonClick(){
        if(metadataPathField.getText().isEmpty()){
            statusLabel.setText("Metadata file not added. Please enter metadata path");}
        else{
            metadata.setFilePath(metadataPathField.getText());
            Navigation.openPopUpWindow("properties-metadata.fxml","Metmap: Metadata",
                    (PropertiesMetadataController controller) -> {
                        controller.setMetadataFile(metadata);
                    });
        }
    }
    @FXML protected void onIndexPropertiesButtonClick(){
        if(indexPathField.getText().isEmpty()){
            statusLabel.setText("Index file not added. Please enter index path");
        }
        else{
            Navigation.openPopUpWindow("properties-index.fxml","Metmap: Index File",
                    (PropertiesIndexController controller) -> {
                        controller.setMetadataFile(metadata); // Pass metadataFile instead
                    });
        }
    }






    @FXML
    protected void onCompileClick() {

        // Re-checking all possible errors and informing
        if (metadata == null || metadataPathField.getText().isEmpty()) {
            statusLabel.setText("Error: Metadata file is missing!");
            return;}
        if (originalSampleFolder == null) {
            statusLabel.setText("Error: Sample folder not set!");
            return;}
        if (processedSampleFolder == null){
            statusLabel.setText("Error: Processed sample folder not set!");
            return;}

        // TODO: Disable buttons so the user doesn't click "Compile" twice
        statusLabel.setText("Initializing...");

        javafx.concurrent.Task<List<DMR>> compileTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<DMR> call() throws Exception {
                // 1. Build Metadata
                updateMessage("Building metadata (this may take several minutes)...");
                Mapper.getInstance().buildMetadata(metadata, metadata.getIndexFilePath());

                // 2. Prepare Samples
                updateMessage("Preparing samples (this may take several minutes)...");
                metadata.setSampleFilePaths(Mapper.getInstance().buildSamples(metadata, originalSampleFolder, processedSampleFolder));

                // 3. Run Filters
                List<DMR> allDiscoveredDMRs = new ArrayList<>();
                List<String> samples = metadata.getSampleFilePaths();
                for (int i = 0; i < activeFilters.size(); i++) {
                    Filter filter = activeFilters.get(i);
                    updateMessage("Running filter: " + filter.getName() + "...");
                    Metmap result = filter.process(metadata, samples);
                    if (result.getDMRs() != null) {
                        List<DMR> filterResults = Arrays.asList(result.getDMRs());
                        List<DMR> collapsedResults = Mapper.getInstance().mergeDMRs(true, new ArrayList<>(), filterResults);
                        allDiscoveredDMRs = Mapper.getInstance().mergeFiltersDMRs(true, allDiscoveredDMRs, collapsedResults);}
                }
                return allDiscoveredDMRs;
            }
        };
        statusLabel.textProperty().bind(compileTask.messageProperty());

        compileTask.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind(); // Stop the binding
            List<DMR> results = compileTask.getValue();
            discoveredDMRs.setAll(results);

            // FOR CANVAS
            long totalGenomeSize = calculateTotalLength();
            double scale = 0.0001; // Adjust this down if your genome is huge
            double calculatedWidth = totalGenomeSize * scale;
            methylationCanvas.setWidth(Math.min(calculatedWidth, 16000));
            drawTiledDmrMap();

            locationCountLabel.setText(results.size() + " Found");
            statusLabel.setText("Compilation Complete.");
        });

        // Handle errors
        compileTask.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error occurred during compilation!");
            compileTask.getException().printStackTrace();
        });

        new Thread(compileTask).start();
    }

    @FXML protected void onExportDMRClick() {
        if (discoveredDMRs.isEmpty()) {
            statusLabel.setText("No DMRs to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save DMR Map");
        fileChooser.setInitialFileName("discovered_dmrs.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv")
        );

        File file = fileChooser.showSaveDialog(Navigation.getStage());

        if (file != null) {
            try {
                DMRExporter.exportToCSV(discoveredDMRs, file);
                statusLabel.setText("Exported " + discoveredDMRs.size() + " DMRs to " + file.getName());
            } catch (Exception e) {
                statusLabel.setText("Export failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --- FILTER LOGIC ---

    @FXML
    protected void onRemoveFilterClick() {
        Filter selected = filterListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            activeFilters.remove(selected);
            statusLabel.setText("Removed filter.");
        } else {
            statusLabel.setText("Please select a filter to remove.");
        }
    }

    @FXML
    protected void onAddSlidingWindowClick() {
        Navigation.openPopUpWindow("filter-sliding-window.fxml", "Add Sliding Window Filter",
                (FilterSlidingWindowController controller) -> {
                    controller.setOnSave(newFilter -> {
                        activeFilters.add(newFilter);
                        statusLabel.setText("Filter added: " + newFilter.getName());
                    });
                });
    }


    // --- MAP VISUALIZATION LOGIC ---

    private static final double TILE_WIDTH = 5000.0; // Safe size for all GPUs

    private void drawTiledDmrMap() {
        canvasContainer.getChildren().clear();

        long totalGenomeSize = calculateTotalLength();
        double scale = 0.00002; // Adjust zoom here
        double totalWidth = totalGenomeSize * scale;
        int numTiles = (int) Math.ceil(totalWidth / TILE_WIDTH);

        for (int i = 0; i < numTiles; i++) {
            Canvas tile = new Canvas(TILE_WIDTH, 200);
            GraphicsContext gc = tile.getGraphicsContext2D();

            double tileStartGlobalX = i * TILE_WIDTH;
            double tileEndGlobalX = (i + 1) * TILE_WIDTH;

            // Draw only DMRs that fall within this tile's boundaries
            for (DMR dmr : discoveredDMRs) {
                double dmrGlobalX = getGlobalPosition(dmr.getChromosome(), dmr.getStartLocation()) * scale;
                double dmrWidth = Math.max((dmr.getEndLocation() - dmr.getStartLocation()) * scale, 1.0);

                // Check if DMR is inside this tile
                if (dmrGlobalX + dmrWidth >= tileStartGlobalX && dmrGlobalX <= tileEndGlobalX) {
                    double localX = dmrGlobalX - tileStartGlobalX;

                    gc.setFill(Color.rgb(52, 152, 219, Math.min(1.0, dmr.getMeanMethylation())));
                    gc.fillRect(localX, 50, dmrWidth, 100);
                }
            }
            canvasContainer.getChildren().add(tile);
        }
    }
    private double calculateGenomeScale(Metadata metadata, double canvasWidth) {
        int[] chromosomes = metadata.getChromosomes();
        int[] locations = metadata.getLocations();

        if (locations == null || locations.length == 0) return 1.0;

        // 1. Find the total length by finding the max location for each chromosome
        // This assumes chromosomes are sorted (which buildMetadata ensures)
        long totalGenomeLength = 0;
        int currentChr = -1;
        int maxLocInChr = 0;

        for (int i = 0; i < chromosomes.length; i++) {
            if (chromosomes[i] != currentChr) {
                totalGenomeLength += maxLocInChr;
                currentChr = chromosomes[i];
                maxLocInChr = 0;
            }
            maxLocInChr = Math.max(maxLocInChr, locations[i]);
        }
        totalGenomeLength += maxLocInChr; // Add the last chromosome

        // 2. Return the ratio of pixels per base pair
        return canvasWidth / (double) totalGenomeLength;
    }

    private long getGlobalPosition(byte chr, int loc) {
        return chromosomeOffsets[chr] + loc; // Returns the pre-calculated offset for this chromosome + the local BP position
    }

    private long calculateTotalLength() {
        int[] chromosomes = metadata.getChromosomes();
        int[] locations = metadata.getLocations();

        if (chromosomes == null || chromosomes.length == 0) return 0;

        long totalLength = 0;
        int currentChr = -1;
        int maxLocInChr = 0;

        for (int i = 0; i < chromosomes.length; i++) {
            // When we encounter a new chromosome, add the previous max location to the total
            if (chromosomes[i] != currentChr) {
                totalLength += maxLocInChr;
                currentChr = chromosomes[i];
                maxLocInChr = 0;
            }
            // Track the highest BP location seen for the current chromosome
            if (locations[i] > maxLocInChr) {
                maxLocInChr = locations[i];
            }
        }
        // Add the final chromosome's length
        totalLength += maxLocInChr;

        return totalLength;
    }

}

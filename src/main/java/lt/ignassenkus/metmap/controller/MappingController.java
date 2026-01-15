package lt.ignassenkus.metmap.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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

    private Metadata metadataFile;
    private List<Sample> sampleFolder = new ArrayList<Sample>();
    private Metmap metmap = new Metmap();
    private ObservableList<Filter> activeFilters = FXCollections.observableArrayList();
    private ObservableList<DMR> discoveredDMRs = FXCollections.observableArrayList();

    @FXML private ListView<Filter> filterListView;
    @FXML private ListView<DMR> locationListView;
    @FXML Label statusLabel;
    @FXML Label locationCountLabel;
    @FXML TextField metadataPathField;
    @FXML TextField indexPathField;
    @FXML TextField samplesPathField;
    @FXML TextField processedSamplesPathField;

    @FXML protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML protected void onBackButtonClick(){Navigation.gotoScene("menu.fxml");}

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
                            item.getChromosome(), item.getStartLocation(), item.getEndLocation()));
                }
            }
        });
    }

    // Browse Buttons
    @FXML protected void onButtonBrowseMetadataClick(){
        metadataFile = new Metadata();
        File chosenFile = getCSVFile();
        if(chosenFile != null){
            metadataPathField.setText(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseIndexPathClick(){
        File chosenFile = getCSVFile();
        if(chosenFile != null){
            indexPathField.setText(chosenFile.getAbsolutePath());
            metadataFile.setIndexFilePath(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseSamplesPathClick(){
        DirectoryChooser folderChooser = new DirectoryChooser();
        File chosenFolder = folderChooser.showDialog(Navigation.getStage());
        if(chosenFolder != null){
            samplesPathField.setText(chosenFolder.getAbsolutePath());
            Mapper.getInstance().setSampleFolder(samplesPathField.getText());
        }
    }
    @FXML protected void onButtonBrowseProcessedSamplesPathClick(){
        DirectoryChooser folderChooser = new DirectoryChooser();
        File chosenFolder = folderChooser.showDialog(Navigation.getStage());
        if(chosenFolder != null){
            processedSamplesPathField.setText(chosenFolder.getAbsolutePath());
            Mapper.getInstance().setProcessedSampleFolder(processedSamplesPathField.getText());
        }
    }

    // Properties Buttons
    @FXML protected void onMetadataPropertiesButtonClick(){
        if(metadataPathField.getText().isEmpty()){
            statusLabel.setText("Metadata file not added. Please enter metadata path");}
        else{
            metadataFile.setFilePath(metadataPathField.getText());
            Navigation.openPopUpWindow("properties-metadata.fxml","Metmap: Metadata",
                    (PropertiesMetadataController controller) -> {
                        controller.setMetadataFile(metadataFile);
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
                        controller.setMetadataFile(metadataFile); // Pass metadataFile instead
                    });
        }
    }






    @FXML
    protected void onCompileClick() {

        // Re-checking all possible errors and informing
        if (metadataFile == null || metadataPathField.getText().isEmpty()) {
            statusLabel.setText("Error: Metadata file is missing!");
            return;
        }
        if (Mapper.getInstance().getSampleFolder() == null) {
            statusLabel.setText("Error: Sample folder not set!");
            return;
        }
        if (Mapper.getInstance().getProcessedSampleFolder() == null){
            statusLabel.setText("Error: Processed sample folder not set!");
            return;
        }

        // Disable buttons so the user doesn't click "Compile" twice
        // (You'll need fx:id for your compile button to do this)
        statusLabel.setText("Initializing...");

        javafx.concurrent.Task<List<DMR>> compileTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<DMR> call() throws Exception {
                // 1. Build Metadata
                updateMessage("Building metadata (this may take a while)...");
                Mapper.getInstance().buildMetadata(metadataFile, metadataFile.getIndexFilePath());
                System.out.println("Metadata built!");

                // 2. Compile Samples
                updateMessage("Compiling samples (this may take a while)...");
                System.out.println(Mapper.getInstance().getSampleFolder());
                System.out.println(Mapper.getInstance().getProcessedSampleFolder());
                metadataFile.setSampleFilePaths(Mapper.getInstance().buildSamples(metadataFile));
                System.out.println("Samples built well!");

                // 3. Run Filters
                List<DMR> allDiscoveredDMRs = new ArrayList<>();
                List<String> samples = metadataFile.getSampleFilePaths();

                for (int i = 0; i < activeFilters.size(); i++) {
                    Filter filter = activeFilters.get(i);
                    updateMessage("Running filter: " + filter.getName() + "...");

                    Metmap result = filter.process(metadataFile, samples);
                    if (result.getDMRs() != null) {
                        allDiscoveredDMRs.addAll(Arrays.asList(result.getDMRs()));
                    }
                }

                // 4. merge and cut(not yet implemented) the DMRs and return them
                allDiscoveredDMRs = Mapper.getInstance().mergeDMRs(allDiscoveredDMRs);
                return allDiscoveredDMRs;
            }
        };

        // Link the statusLabel to the Task's message
        statusLabel.textProperty().bind(compileTask.messageProperty());

        // What to do when finished successfully
        compileTask.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind(); // Stop the binding
            List<DMR> results = compileTask.getValue();
            discoveredDMRs.setAll(results);
            locationCountLabel.setText(results.size() + " Found");
            statusLabel.setText("Compilation Complete.");
        });

        // Handle errors
        compileTask.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error occurred during compilation!");
            compileTask.getException().printStackTrace();
        });

        // Start the background thread
        new Thread(compileTask).start();
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

    @FXML
    protected void onExportDMRClick() {
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
}

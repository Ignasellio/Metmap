package lt.ignassenkus.metmap.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import lt.ignassenkus.metmap.service.Mapper;
import lt.ignassenkus.metmap.service.Navigation;
import lt.ignassenkus.metmap.service.filter.Filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MappingController {

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

    @FXML protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML protected void onBackButtonClick(){Navigation.gotoScene("menu.fxml");}

    private File getCSVFile(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);
        return fileChooser.showOpenDialog(Navigation.getStage());
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
    public void initialize() {
        // Setup filter list (as you had it)
        filterListView.setItems(activeFilters);
        filterListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Filter item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText(item.getName()); }
            }
        });

        // 4. SETUP the DMR list
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



    // --- WTF IS GOING ON FROM HERE?? ---

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
    protected void onCompileClick() {
        // Disable buttons so the user doesn't click "Compile" twice
        // (You'll need fx:id for your compile button to do this)
        statusLabel.setText("Initializing...");

        javafx.concurrent.Task<List<DMR>> compileTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<DMR> call() throws Exception {
                // 1. Build Metadata
                updateMessage("Building metadata (this may take a while)...");
                Mapper.getInstance().buildMetadata(metadataFile, metadataFile.getIndexFilePath());

                // 2. Compile Samples
                updateMessage("Compiling samples (this may take a while)...");
                metadataFile.setSampleFilePaths(Mapper.getInstance().compileSamples(metadataFile, samplesPathField.getText()));

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

    @FXML
    protected void onAddSlidingWindowClick() {
        Navigation.openPopUpWindow("filter-sliding-window.fxml", "Add Sliding Window Filter",
                (FilterSlidingWindowController controller) -> {
                    // Pass a consumer that adds the result to our list
                    controller.setOnSave(newFilter -> {
                        activeFilters.add(newFilter);
                        statusLabel.setText("Filter added: " + newFilter.getName());
                    });
                });
    }

}

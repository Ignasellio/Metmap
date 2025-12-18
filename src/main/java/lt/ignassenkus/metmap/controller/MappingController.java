package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.model.MethNames;
import lt.ignassenkus.metmap.model.Sample;
import lt.ignassenkus.metmap.util.Navigation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MappingController {

    private Metadata metadataFile;
    private MethNames indexFile;
    private List<Sample> sampleFolder = new ArrayList<Sample>();;

    @FXML Label statusLabel;
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

    // 3 Browse Buttons
    @FXML protected void onButtonBrowseMetadataClick(){
        metadataFile = new Metadata();
        File chosenFile = getCSVFile();
        if(chosenFile != null){
            metadataPathField.setText(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseIndexPathClick(){
        indexFile = new MethNames();
        File chosenFile = getCSVFile();
        if(chosenFile != null){
            indexPathField.setText(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseSamplesPathClick(){
        DirectoryChooser folderChooser = new DirectoryChooser();
        File chosenFolder = folderChooser.showDialog(Navigation.getStage());
        if(chosenFolder != null){
            samplesPathField.setText(chosenFolder.getAbsolutePath());
        }
    }

    // 3 Properties Buttons
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
            indexFile.setFileName(indexPathField.getText());
            Navigation.openPopUpWindow("properties-index.fxml","Metmap: Index File",
                    (PropertiesIndexController controller) -> {
                        controller.setIndexFile(indexFile);
                    });
        }
    }

    @FXML protected void onCompileClick(){
        statusLabel.setText("Please wait. Loading metadata file...");
        statusLabel.setText("Please wait. Loading index file...");
        statusLabel.setText("Please wait. Standardizing sample files (1/999)...");
        // For LOOP kiekis filtrų
        statusLabel.setText("Please wait. Applying filter (1/99)...");
    }

}

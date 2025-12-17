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

public class MappingController {

    private Metadata metadataFile = new Metadata();
    private MethNames indexFile;
    private Sample[] samplesFolder;

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
        File chosenFile = getCSVFile();
        if(chosenFile != null){
            metadataPathField.setText(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseIndexPathClick(){
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
            //Navigation.openWindow("properties-metadata.fxml","Metmap: Metadata");
            Navigation.openPopUpWindow("properties-metadata.fxml","Metmap: Metadata",
                    (PropertiesMetadataController controller) -> {
                        controller.setMetadataFile(metadataFile);
                    });
        }
    }
    @FXML protected void onIndexButtonClick(){
        if(indexPathField.getText().isEmpty()){
            statusLabel.setText("Index file not added. Please enter index path");
        }
        else{
            Navigation.openWindow("index-metadata.fxml","Metmap: Index");
        }
    }
    @FXML protected void onSamplesButtonClick(){
        if(samplesPathField.getText().isEmpty()){
            statusLabel.setText("Sample folder not added. Please enter sample path");
        }
        else{
            Navigation.openWindow("sample-metadata.fxml","Metmap: Samples");
        }
    }
}

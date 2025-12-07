package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lt.ignassenkus.metmap.util.Navigation;

import java.io.File;

public class MappingController {

    @FXML Label statusLabel;
    @FXML TextField metadataPathField;
    @FXML TextField indexPathField;
    @FXML TextField samplesPathField;

    @FXML protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML protected void onBackButtonClick(){Navigation.gotoScene("menu.fxml");}

    @FXML protected void onButtonBrowseMetadataClick(){
        FileChooser fileChooser = new FileChooser();
        File chosenFile = fileChooser.showOpenDialog(Navigation.getStage());
        if(chosenFile != null){
            metadataPathField.setText(chosenFile.getAbsolutePath());
        }
    }
    @FXML protected void onButtonBrowseIndexPathClick(){
        FileChooser fileChooser = new FileChooser();
        File chosenFile = fileChooser.showOpenDialog(Navigation.getStage());
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

    @FXML protected void onMetadataPropertiesButtonClick(){
        if(metadataPathField.getText().isEmpty()){
            statusLabel.setText("Metadata file not added. Please enter metadata path");
        }
        else{Navigation.openWindow("properties-metadata.fxml","Metmap: Metadata");}
    }
    @FXML protected void onIndexButtonClick(){
        if(indexPathField.getText().isEmpty()){
            statusLabel.setText("Index file not added. Please enter index path");
        }
        else{Navigation.openWindow("index-metadata.fxml","Metmap: Index");}
    }
    @FXML protected void onSamplesButtonClick(){
        if(samplesPathField.getText().isEmpty()){
            statusLabel.setText("Sample folder not added. Please enter sample path");
        }
        else{Navigation.openWindow("sample-metadata.fxml","Metmap: Samples");}
    }
}

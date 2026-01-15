package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import lt.ignassenkus.metmap.service.Navigation;
import lt.ignassenkus.metmap.service.Settings;

public class SettingsController {
    @FXML private CheckBox CheckToVisualize;
    @FXML private CheckBox CheckToEmptyFolder;

    @FXML public void initialize(){
        if(Settings.toVisualize()){CheckToVisualize.setSelected(true);
        } else {CheckToVisualize.setSelected(false);}
        if(Settings.toEmptyFolder()){CheckToEmptyFolder.setSelected(true);
        } else{CheckToEmptyFolder.setSelected(false);}
    }

    @FXML protected void onBackButtonClick(){
        Settings.getInstance().setToVisualize(CheckToVisualize.selectedProperty().get());
        Settings.getInstance().setToEmptyFolder(CheckToEmptyFolder.selectedProperty().get());
        Navigation.gotoPrevScene();
    }
}

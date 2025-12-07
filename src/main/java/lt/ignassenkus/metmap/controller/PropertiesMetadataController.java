package lt.ignassenkus.metmap.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lt.ignassenkus.metmap.util.Navigation;

public class PropertiesMetadataController {
    @FXML
    protected void onButtonApplyAndClose(ActionEvent event){
        Navigation.closeWindow(event);
    }
}

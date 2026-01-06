package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import lt.ignassenkus.metmap.service.Navigation;

public class SettingsController {
    @FXML
    protected void onBackButtonClick(){Navigation.gotoPrevScene();}
}

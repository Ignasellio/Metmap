package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import lt.ignassenkus.metmap.util.Navigation;

public class SettingsController {
    @FXML
    protected void onBackButtonClick(){Navigation.gotoPrevScene();}
}

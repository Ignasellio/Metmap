package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lt.ignassenkus.metmap.util.Navigation;

public class MappingController {
    @FXML
    Label statusLabel;
    @FXML
    protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML
    protected void onBackButtonClick(){Navigation.gotoScene("menu.fxml");}
    }

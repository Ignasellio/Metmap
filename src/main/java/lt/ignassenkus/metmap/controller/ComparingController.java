package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import lt.ignassenkus.metmap.util.Navigation;

public class ComparingController {
    @FXML
    protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML
    protected void onBackButtonClick(){Navigation.gotoScene("menu.fxml");}
}

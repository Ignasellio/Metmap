package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lt.ignassenkus.metmap.util.Navigation;

public class MenuController {
    @FXML
    protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML
    protected void onMappingButtonClick() {Navigation.gotoScene("mapping.fxml");}
    @FXML
    protected void onComparingButtonClick() {Navigation.gotoScene("comparing.fxml");}
}

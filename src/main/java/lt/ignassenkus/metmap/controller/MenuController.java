package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lt.ignassenkus.metmap.util.Navigation;

public class MenuController {
    @FXML
    private Label welcomeText;
    @FXML
    protected void onMappingButtonClick() {Navigation.showScene("mapping.fxml");}
    @FXML
    protected void onComparingButtonClick() {Navigation.showScene("comparing.fxml");}
}

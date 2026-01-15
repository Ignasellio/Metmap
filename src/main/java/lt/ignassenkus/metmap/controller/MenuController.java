package lt.ignassenkus.metmap.controller;

import javafx.fxml.FXML;
import lt.ignassenkus.metmap.service.Navigation;

public class MenuController {
    @FXML protected void onSettingsButtonClick(){Navigation.gotoScene("settings.fxml");}
    @FXML protected void onMappingButtonClick(){Navigation.gotoScene("mapping.fxml");}
    @FXML protected void onComparingButtonClick(){Navigation.gotoScene("comparing.fxml");}
}
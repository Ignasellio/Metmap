package lt.ignassenkus.metmap;

import javafx.application.Application;
import javafx.stage.Stage;
import lt.ignassenkus.metmap.service.Navigation;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Metmap");
        Navigation.setStage(stage);
        Navigation.gotoScene("menu.fxml");
    }
}

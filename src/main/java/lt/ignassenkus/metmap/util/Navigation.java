package lt.ignassenkus.metmap.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Navigation {

    private static Stage stage;
    public static void setStage(Stage primaryWindow) {
        stage = primaryWindow;
    }

    private static String PrevSceneFXMLFileName;
    public static String getPrevSceneFXMLFileName() {
        return PrevSceneFXMLFileName;
    }

    // main function used to switch between scenes
    public static void showScene(String fxmlFileName) {
        try {
            // Loading the FXML file
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("/lt/ignassenkus/metmap/view/" + fxmlFileName));
            Parent root = loader.load();

            // Creating a new scene (or update the existing one) and playing it
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Could not load FXML file: " + fxmlFileName);
            e.printStackTrace();
        }
    }
}

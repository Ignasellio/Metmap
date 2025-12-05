package lt.ignassenkus.metmap.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class Navigation {

    static final String FXML_PATH = "/lt/ignassenkus/metmap/view/";
    private static String CurrSceneFXMLFileName;
    private static String PrevSceneFXMLFileName;
    private static Stage primaryStage;
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // main function used to switch between scenes
    public static void gotoScene(String fxmlFileName) {
        try {
            // Loading the FXML file
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(FXML_PATH + fxmlFileName));
            Parent root = loader.load();
            // Creating a new scene (or update the existing one) and playing it
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Could not load FXML file: " + fxmlFileName);
            e.printStackTrace();
        }
        // Adjusting values of current and previous Scene (for ability to go back)
        if(CurrSceneFXMLFileName != null) {PrevSceneFXMLFileName = CurrSceneFXMLFileName;}
        CurrSceneFXMLFileName = fxmlFileName;
    }

    public static void gotoPrevScene(){
        gotoScene(PrevSceneFXMLFileName);
    }

    public static void openWindow(String fxmlFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(FXML_PATH + fxmlFileName));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));

            // Makes it so you can't click the main window until this one closes
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(primaryStage);

            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

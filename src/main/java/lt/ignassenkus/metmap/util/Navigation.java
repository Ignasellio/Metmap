package lt.ignassenkus.metmap.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.function.Consumer;

public class Navigation {

    static final String FXML_PATH = "/lt/ignassenkus/metmap/view/";
    private static String CurrSceneFXMLFileName;
    private static String PrevSceneFXMLFileName;
    private static Stage primaryStage;
    public static Stage getStage() {
        return primaryStage;
    }
    public static void setStage(Stage stage) {
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

    public static <T> void openPopUpWindow(String fxmlPath, String title, Consumer<T> controllerSetup){
        try {
            // 1. Load the FXML
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(FXML_PATH + fxmlPath));
            Parent root = loader.load();
            // 2. Get the Controller (Generic T casts it automatically)
            T controller = loader.getController();
            // 3. Execute the consumer logic (pass data)
            if (controllerSetup != null) {
                controllerSetup.accept(controller);
            }
            // 4. Show the Stage
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeWindow(javafx.event.ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}

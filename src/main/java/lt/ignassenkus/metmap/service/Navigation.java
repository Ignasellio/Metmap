package lt.ignassenkus.metmap.service;

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

    // USAGE: switch between scenes in the same stage, no possibility of transferring data
    public static void gotoScene(String fxmlFileName) {
        // We call the main method and pass 'true' as the default
        gotoScene(fxmlFileName, false);
    }
    public static void gotoScene(String fxmlFileName, boolean isFullScreen) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(FXML_PATH + fxmlFileName));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(isFullScreen);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Could not load FXML file: " + fxmlFileName);
            e.printStackTrace();
        }
        // Adjusting values of current and previous Scene (for ability to go back)
        if(CurrSceneFXMLFileName != null) {PrevSceneFXMLFileName = CurrSceneFXMLFileName;}
        CurrSceneFXMLFileName = fxmlFileName;
    }

    // Should be substituted with gotoScene, because doesn't update Prev/Curr Scenes
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
            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(FXML_PATH + fxmlPath));
            Parent root = loader.load();

            T controller = loader.getController();
            if (controllerSetup != null) {
                controllerSetup.accept(controller);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            // Makes it so you can't click the main window until this one closes
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(primaryStage);

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

package lt.ignassenkus.metmap.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lt.ignassenkus.metmap.service.Mapper;
import lt.ignassenkus.metmap.service.Navigation;

import java.io.File;

public class MenuController {

    @FXML
    private TextField folderPathField;

    @FXML
    private Button mapButton;

    @FXML
    private Button compareButton;

    @FXML
    public void initialize() {
        folderPathField.setText(Mapper.getInstance().getSampleFolder());

        // BINDING LOGIC:
        // Disable buttons if the text field is empty
        mapButton.disableProperty().bind(
                Bindings.createBooleanBinding(() ->
                                folderPathField.getText() == null || folderPathField.getText().trim().isEmpty(),
                        folderPathField.textProperty()
                )
        );

        compareButton.disableProperty().bind(
                Bindings.createBooleanBinding(() ->
                                folderPathField.getText() == null || folderPathField.getText().trim().isEmpty(),
                        folderPathField.textProperty()
                )
        );
    }

    @FXML
    protected void onSelectFolderClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Sample Output Folder");
        Stage stage = (Stage) folderPathField.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            String newPath = selectedDirectory.getAbsolutePath();
            folderPathField.setText(newPath);
            Mapper.getInstance().setSampleFolder(newPath);
        }
    }

    @FXML
    protected void onSettingsButtonClick(){ Navigation.gotoScene("settings.fxml"); } //

    @FXML
    protected void onMappingButtonClick() { Navigation.gotoScene("mapping.fxml"); } //

    @FXML
    protected void onComparingButtonClick() { Navigation.gotoScene("comparing.fxml"); } //
}
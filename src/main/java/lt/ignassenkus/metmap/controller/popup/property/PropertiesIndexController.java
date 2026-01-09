package lt.ignassenkus.metmap.controller.popup.property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.service.CSVManager;
import lt.ignassenkus.metmap.service.Navigation;

public class PropertiesIndexController {

    @FXML Label statusLabel;
    @FXML Label labelAbove;
    @FXML Label labelChosen;
    @FXML Label labelBelow;
    @FXML TextField startRowIndexField;

    Metadata metadataFile;

    public void setMetadataFile(Metadata metadataFile) {
        this.metadataFile = metadataFile;
        if(metadataFile.getIndexesStartRowIndex() != null){
            startRowIndexField.setText(metadataFile.getIndexesStartRowIndex().toString());
            onButtonApply();
        }
    }

    public void onButtonApply() {
        String rawText = startRowIndexField.getText();
        if (rawText == null || rawText.trim().isEmpty()) {
            statusLabel.setText("Starting row index field is empty.");
            return;
        }
        try {
            int rowIndex = Integer.parseInt(rawText);
            metadataFile.setIndexesStartRowIndex(rowIndex);

            labelAbove.setText(CSVManager.readRow(
                    metadataFile.getIndexFilePath(), // Using path from metadata
                    metadataFile.getIndexesStartRowIndex()-1,
                    null, null).toString());
            labelChosen.setText(CSVManager.readRow(
                    metadataFile.getIndexFilePath(),
                    metadataFile.getIndexesStartRowIndex(),
                    null, null).toString());
            labelBelow.setText(CSVManager.readRow(
                    metadataFile.getIndexFilePath(),
                    metadataFile.getIndexesStartRowIndex()+1,
                    null, null).toString());

            if(rowIndex == 0){labelAbove.setText("<DOES NOT EXIST>");}

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Row index input. Please enter a valid number.");
        }
    }

    @FXML
    protected void onButtonApplyAndClose(ActionEvent event){
        boolean wasError = false;
        if (startRowIndexField.getText() == null || startRowIndexField.getText().trim().isEmpty()) {
            statusLabel.setText("Header row index field is empty. Please enter a valid row index.");
            wasError = true;
        }
        if (!wasError){Navigation.closeWindow(event);}
    }



}

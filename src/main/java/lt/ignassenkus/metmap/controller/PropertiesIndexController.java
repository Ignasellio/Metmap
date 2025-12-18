package lt.ignassenkus.metmap.controller;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lt.ignassenkus.metmap.model.MethNames;
import lt.ignassenkus.metmap.util.CSVReader;
import lt.ignassenkus.metmap.util.Navigation;

import java.util.stream.Collectors;

public class PropertiesIndexController {

    @FXML Label statusLabel;
    @FXML Label labelAbove;
    @FXML Label labelChosen;
    @FXML Label labelBelow;
    @FXML TextField startRowIndexField;

    MethNames indexFile;

    public void setIndexFile(MethNames indexFile) {
        this.indexFile = indexFile;
        if(indexFile.getStartingRowIndex() != null){
            startRowIndexField.setText(indexFile.getStartingRowIndex().toString());
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
            indexFile.setStartingRowIndex(rowIndex);

            labelAbove.setText(CSVReader.readRow(
                    indexFile.getFileName(),
                    indexFile.getStartingRowIndex()-1,
                    null,
                    null).toString());
            labelChosen.setText(CSVReader.readRow(
                    indexFile.getFileName(),
                    indexFile.getStartingRowIndex(),
                    null,
                    null).toString());
            labelBelow.setText(CSVReader.readRow(
                    indexFile.getFileName(),
                    indexFile.getStartingRowIndex()+1,
                    null,
                    null).toString());

            //Dealing with labels if highest line is picked (lowest will cause catch block)
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

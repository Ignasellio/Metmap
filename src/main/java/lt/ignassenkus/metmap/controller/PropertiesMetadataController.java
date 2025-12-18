package lt.ignassenkus.metmap.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lt.ignassenkus.metmap.model.Metadata;
import lt.ignassenkus.metmap.util.CSVReader;
import lt.ignassenkus.metmap.util.Navigation;

import java.util.List;
import java.util.stream.Collectors;

public class PropertiesMetadataController {

    private Metadata metadataFile;

    @FXML Label statusLabel;
    @FXML Label headerRowContent;
    @FXML TextField headerRowIndexField;
    @FXML ComboBox<String> nameBox;
    @FXML ComboBox<String> chrBox;
    @FXML ComboBox<String> locBox;

    List<String> headerValues;

    public void setMetadataFile(Metadata metadataFile) {
        this.metadataFile = metadataFile;

        if(metadataFile.getHeaderRowIndex() != null){

            headerRowIndexField.setText(metadataFile.getHeaderRowIndex().toString());
            onButtonApplyRowIndex();

            if(metadataFile.getChromosomeColumnIndex() != null){
                chrBox.getSelectionModel().select(this.metadataFile.getChromosomeColumnIndex());
            }
            if(metadataFile.getNameColumnIndex() != null){
                nameBox.getSelectionModel().select(this.metadataFile.getNameColumnIndex());
            }
            if(metadataFile.getLocationColumnIndex() != null){
                locBox.getSelectionModel().select(this.metadataFile.getLocationColumnIndex());
            }
        }
    }

    @FXML
    protected void onButtonApplyRowIndex(){
        String rawText = headerRowIndexField.getText();
        if (rawText == null || rawText.trim().isEmpty()) {
            statusLabel.setText("Header row index field is empty.");
            return;
        }
        try {
            int rowIndex = Integer.parseInt(rawText);
            headerValues = CSVReader.readRow(metadataFile.getFilePath(), rowIndex, null, null);
            if(headerValues.size() < 3){
                statusLabel.setText("This row has less than 3 columns (" + headerValues.size() +"), this cannot be the header row.");
            }
            else{
                String joinedText = headerValues.stream().limit(5).collect(Collectors.joining(", "));
                headerRowContent.setText(joinedText);
                nameBox.getSelectionModel().clearSelection();
                nameBox.setItems(FXCollections.observableArrayList(headerValues));
                chrBox.getSelectionModel().clearSelection();
                chrBox.setItems(FXCollections.observableArrayList(headerValues));
                locBox.getSelectionModel().clearSelection();
                locBox.setItems(FXCollections.observableArrayList(headerValues));
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid Row index input. Please enter a valid number.");
        }
    }

    @FXML
    protected void onButtonApplyAndClose(ActionEvent event){
        boolean wasError = false;
        if (headerRowIndexField.getText() == null || headerRowIndexField.getText().trim().isEmpty()) {
            statusLabel.setText("Header row index field is empty. Please enter a valid row index.");
            wasError = true;
        }
        else{metadataFile.setHeaderRowIndex(Integer.parseInt(headerRowIndexField.getText()));}
        if (nameBox.getSelectionModel().getSelectedItem() == null){
            statusLabel.setText("Name column not selected. Please select a name column.");
            wasError = true;
        }
        else{metadataFile.setNameColumnIndex(nameBox.getSelectionModel().getSelectedIndex());}
        if (chrBox.getSelectionModel().getSelectedItem() == null){
            statusLabel.setText("Chromosome column not selected. Please select a chromosome column.");
            wasError = true;
        }
        else{metadataFile.setChromosomeColumnIndex(chrBox.getSelectionModel().getSelectedIndex());}
        if (locBox.getSelectionModel().getSelectedItem() == null){
            statusLabel.setText("Location column not selected. Please select a location column.");
            wasError = true;
        }
        else{metadataFile.setLocationColumnIndex(locBox.getSelectionModel().getSelectedIndex());}

        if (!wasError){Navigation.closeWindow(event);}

    }

}

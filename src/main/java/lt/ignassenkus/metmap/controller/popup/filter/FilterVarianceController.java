package lt.ignassenkus.metmap.controller.popup.filter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lt.ignassenkus.metmap.service.filter.Filter;
import lt.ignassenkus.metmap.service.filter.FilterVariance;
import lt.ignassenkus.metmap.service.Navigation;
import java.util.function.Consumer;

public class FilterVarianceController {

    @FXML private Label statusLabel;
    @FXML private TextField filterNameField;
    @FXML private TextField windowSizeField;
    @FXML private TextField minPointsField;
    @FXML private TextField varianceThresholdField;

    private Consumer<Filter> onSaveCallback;

    public void setOnSave(Consumer<Filter> callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    protected void onButtonConfirmAndClose(ActionEvent event) {
        try {
            String baseName = filterNameField.getText().trim();
            if (baseName.isEmpty()) baseName = "Variance Filter";
            String fullName = String.format("%s [Size: %s, Min CpGs: %s, Var: %s]",
                    baseName,
                    windowSizeField.getText().trim(),
                    minPointsField.getText().trim(),
                    varianceThresholdField.getText().trim());

            int windowSize = Integer.parseInt(windowSizeField.getText().trim());
            int minPoints = Integer.parseInt(minPointsField.getText().trim());
            float varThreshold = Float.parseFloat(varianceThresholdField.getText().trim());

            FilterVariance filter = new FilterVariance(fullName, windowSize, minPoints, varThreshold);
            if (onSaveCallback != null) {
                onSaveCallback.accept(filter);
            }

            Navigation.closeWindow(event);

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid numeric input. Please check your values.");
        }
    }
}

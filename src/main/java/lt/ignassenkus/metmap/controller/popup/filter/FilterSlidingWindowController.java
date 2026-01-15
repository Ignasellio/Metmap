package lt.ignassenkus.metmap.controller.popup.filter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lt.ignassenkus.metmap.service.filter.Filter;
import lt.ignassenkus.metmap.service.filter.FilterSlidingWindow;
import lt.ignassenkus.metmap.service.Navigation;
import java.util.function.Consumer;

public class FilterSlidingWindowController {

    @FXML private Label statusLabel;
    @FXML private TextField filterNameField;
    @FXML private TextField windowSizeField;
    @FXML private TextField minPointsField;
    @FXML private TextField minHighMetField;
    @FXML private TextField minDiffField;

    private Consumer<Filter> onSaveCallback;

    /**
     * Sets the callback to be executed when the user confirms the filter settings.
     */
    public void setOnSave(Consumer<Filter> callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    protected void onButtonConfirmAndClose(ActionEvent event) {
        try {
            // Validate inputs
            String name = filterNameField.getText().trim() + "["
                    + "window size: " + windowSizeField.getText().trim() + " "
                    + " min. CpGs in window: " + minPointsField.getText().trim()
                    + " high meth. level: " + minHighMetField.getText().trim()
                    + " min. high meth. percent: " + minDiffField.getText().trim() + "]";
            int windowSize = Integer.parseInt(windowSizeField.getText().trim());
            int minPoints = Integer.parseInt(minPointsField.getText().trim());
            float minHighMet = Float.parseFloat(minHighMetField.getText().trim());
            float minDiff = Float.parseFloat(minDiffField.getText().trim());

            if (name.isEmpty()) name = "Sliding Window Filter";

            // Create the stateful filter
            FilterSlidingWindow filter = new FilterSlidingWindow(name, windowSize, minPoints, minHighMet, minDiff);

            // Pass the filter back to the main controller
            if (onSaveCallback != null) {
                onSaveCallback.accept(filter);
            }

            Navigation.closeWindow(event);

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid numeric input. Please check your values.");
        }
    }
}

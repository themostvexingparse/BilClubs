import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;

public class ProgressController {

    @FXML
    private Arc progressArc;

    @FXML
    private Label percentageLabel;


    public void setProgress(double percentage) {
        double clampedPercentage = Math.max(0, Math.min(100, percentage));

        percentageLabel.setText(String.format("%.0f%%", clampedPercentage));

        double length = (clampedPercentage / 100.0) * 360;
        progressArc.setLength(length);
    }
}
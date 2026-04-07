import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.stage.*;
import javafx.scene.*;
import javafx.util.Duration;
import java.io.IOException;

public class LoadHelper {

    public static void safelyLoad(FXMLLoader fxml, Stage stage) throws IOException {
        Parent root = fxml.load();
        
        double width = stage.isMaximized() ? stage.getWidth() : 1280;
        double height = stage.isMaximized() ? stage.getHeight() : 720;
        
        Scene page = new Scene(root, width, height);
        stage.setScene(page);

        stage.maximizedProperty().addListener((obs, wasMax, isMax) -> {
            double baseWidth = 1280.0;
            double baseHeight = 720.0;

            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();

            double currentWidthRate = screenWidth / baseWidth;
            double currentHeightRate = screenHeight / baseHeight;

            double scaleRate;

            if (isMax) {
                scaleRate = Math.min(currentWidthRate, currentHeightRate);
            } else {
                scaleRate = 1.0;
            }

            Parent sceneRoot = stage.getScene().getRoot();
            sceneRoot.setScaleX(scaleRate);
            sceneRoot.setScaleY(scaleRate);

            if (isMax) {
                sceneRoot.setTranslateX((screenWidth - baseWidth) / 2);
                sceneRoot.setTranslateY((screenHeight - baseHeight) / 2);
            } else {
                sceneRoot.setTranslateX(0);
                sceneRoot.setTranslateY(0);
            }
        });

        FadeTransition fade = new FadeTransition(Duration.millis(300), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
package bilclubs.utils;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import bilclubs.controllers.Controller;

public class LoadHelper {

    public static void safelyLoad(FXMLLoader fxml, Stage stage) throws IOException{
        Parent root = fxml.load();

        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                Scene page = new Scene(root);

                if(Controller.isDarkMode){
                    if(!page.getRoot().getStyleClass().contains("darkmode")) {
                        page.getRoot().getStyleClass().add("darkmode");
                    }
                }

                stage.setScene(page);

                FadeTransition fade = new FadeTransition(Duration.millis(300), root);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }

        });
        
    }

    public static void safelyLoad(FXMLLoader fxml, AnchorPane pane) throws IOException {
        Parent root = fxml.load();

        Platform.runLater(() -> {
            root.setOpacity(0);
            pane.getChildren().setAll(root);

            if (Controller.isDarkMode) {
                if(!pane.getScene().getRoot().getStyleClass().contains("darkmode")) {
                    pane.getScene().getRoot().getStyleClass().add("darkmode");
                }
            }

            FadeTransition fade = new FadeTransition(Duration.millis(300), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
    }


}
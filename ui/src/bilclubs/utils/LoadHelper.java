package bilclubs.utils;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.AnchorPane;

import bilclubs.controllers.Controller;

public class LoadHelper {

    public static void safelyLoad(FXMLLoader fxml, Stage stage) throws IOException{
        Parent root = fxml.load();

        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                Scene page = new Scene(root);

                if(Controller.isDarkMode){
                    page.getRoot().getStyleClass().add("darkmode");
                }

                

                stage.setScene(page);
            }
        });

        
    }

    public static void safelyLoad(FXMLLoader fxml, AnchorPane pane) throws IOException{
        Parent root = fxml.load();

        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                pane.getChildren().setAll(root);

                if(Controller.isDarkMode){
                    pane.getStyleClass().add("darkmode");
                }
            }
        });
    }

}
package bilclubs;

import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

import bilclubs.controllers.Controller;

public class App extends Application{

    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/welcomescenebuilder.fxml"));
        Scene scene = new Scene(root); 
        // String css = this.getClass().getResource("styles/styles.css").toExternalForm();
        // scene.getStylesheets().add(css);
        stage.setScene(scene); 
        Controller.backscenes.add(scene);

        // stage.maximizedProperty().addListener((obs, wasMax, isMax) -> {
        //     double currentWidth = 1280.0;
        //     double currentHeight = 720.0;

        //     double currentWidthRate = Screen.getPrimary().getBounds().getWidth()/currentWidth;
        //     double currentHeightRate = Screen.getPrimary().getBounds().getHeight()/currentHeight;

        //     double scaleRate;
        //     Parent sceneRoot = stage.getScene().getRoot();

        //     if(isMax){
        //         scaleRate = Math.min(currentWidthRate,currentHeightRate);
        //         sceneRoot.setTranslateX((Screen.getPrimary().getBounds().getWidth() - 1280.0) / 2);
        //         sceneRoot.setTranslateY((Screen.getPrimary().getBounds().getHeight() - 720.0) / 2);
        //     } else {
        //         scaleRate = 1.0;
        //         sceneRoot.setTranslateX(0);
        //         sceneRoot.setTranslateY(0);
        //     }

        // });

        // stage.maximizedProperty().addListener((obs, wasMax, isMax) -> {
        //     double currentWidth = 1280.0;
        //     double currentHeight = 720.0;

        //     double currentWidthRate = Screen.getPrimary().getBounds().getWidth()/currentWidth;
        //     double currentHeightRate = Screen.getPrimary().getBounds().getHeight()/currentHeight;

        //     double scaleRate;

        //     if(isMax){
        //         scaleRate = Math.min(currentWidthRate,currentHeightRate);
        //     } else {
        //         scaleRate = 1.0;
        //     }

        //     Parent sceneRoot = stage.getScene().getRoot();
        //     sceneRoot.setScaleX(scaleRate);
        //     sceneRoot.setScaleY(scaleRate);

        //     if(isMax){
        //         sceneRoot.setTranslateX((Screen.getPrimary().getBounds().getWidth() - 1280.0) / 2);
        //         sceneRoot.setTranslateY((Screen.getPrimary().getBounds().getHeight() - 720.0) / 2);
        //     } else {
        //         sceneRoot.setTranslateX(0);
        //         sceneRoot.setTranslateY(0);
        //     }
        // });

        // stage.maximizedProperty().addListener((obs, wasMax, isMax) -> {
        //     double scaleX = Screen.getPrimary().getBounds().getWidth() / 1280.0;
        //     double scaleY = Screen.getPrimary().getBounds().getHeight() / 720.0;
        //     double scale = isMax ? Math.min(scaleX, scaleY) : 1.0;
            
        //     Parent sceneRoot = stage.getScene().getRoot();
        //     sceneRoot.setScaleX(scale);
        //     sceneRoot.setScaleY(scale);
            
        //     if (isMax) {
        //         // make the scene match the screen size
        //         sceneRoot.setTranslateX((Screen.getPrimary().getBounds().getWidth() - 1280.0) / 2);
        //         sceneRoot.setTranslateY((Screen.getPrimary().getBounds().getHeight() - 720.0) / 2);
        //     } else {
        //         sceneRoot.setTranslateX(0);
        //         sceneRoot.setTranslateY(0);
        //     }
        // });   

        stage.show();     
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

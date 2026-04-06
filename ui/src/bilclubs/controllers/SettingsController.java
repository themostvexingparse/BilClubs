package bilclubs.controllers;

import java.io.IOException;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class SettingsController {

    @FXML Button darkModeToggle;

    public void toggle(ActionEvent e){
        Controller.isDarkMode = !Controller.isDarkMode;

        Scene scene = darkModeToggle.getScene();
        ObservableList<String> classes = scene.getRoot().getStyleClass();

        if (classes.contains("darkmode")) {
            classes.remove("darkmode");
        } 
        else {
            classes.add("darkmode");
        }
    }
}

package bilclubs.controllers;

import java.io.IOException;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;

public class SettingsController {
    public static boolean isVisible=false;
    public static boolean isManaged=false;

    @FXML Button darkModeToggle;
    @FXML Button accountButton;
    @FXML Button notificationsButton;
    @FXML VBox accountBox;
    @FXML VBox alertBox;

    @FXML
    public void initialize() throws IOException{
        accountBox.setVisible(isVisible);
        accountBox.setManaged(isManaged);
        alertBox.setVisible(isVisible);
        alertBox.setManaged(isManaged);
    }
    @FXML
    public void account(ActionEvent e) throws IOException{
        accountBox.setVisible(isVisible);
        accountBox.setManaged(isManaged);
        isVisible=!isVisible;
        isManaged=!isManaged;
    }
    @FXML
    public void notifications(ActionEvent e) throws IOException{
        System.out.println("Notifications clicked!");
        alertBox.setVisible(isVisible);
        alertBox.setManaged(isManaged);
        isVisible=!isVisible;
        isManaged=!isManaged;
    }

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

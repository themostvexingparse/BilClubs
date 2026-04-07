package bilclubs.controllers;

import java.util.ArrayList;

import bilclubs.components.NotificationCard;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AlertsController {
    @FXML VBox alertsVBox;
    @FXML Label titleLbl;

    //holds notification cards
    public static ArrayList<NotificationCard> allNotifs = new ArrayList<>();

    @FXML
    public void initialize(){
        titleLbl.setText("Alerts");

        if (allNotifs == null) return;

        for(NotificationCard aCard : allNotifs){
            alertsVBox.getChildren().add(aCard);
        }

    }
}

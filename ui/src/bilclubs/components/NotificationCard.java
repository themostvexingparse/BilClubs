package bilclubs.components;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class NotificationCard extends Pane {

    //default messages that'll be displayed
    public static String joinMessage = "You joined a club!";
    public static String leaveMessage = "You left a club.";
    public static String banMessage = "You have been banned.";

    @FXML private Label mainMessageLabel;
    @FXML private Label detailLabel;
    
    @FXML 
    public void initialise() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/notifCard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public NotificationCard(String message, String details) throws IOException{
        initialise();

        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                mainMessageLabel.setText(message);
                detailLabel.setText(details);
            }

        });
        
    }

    
}

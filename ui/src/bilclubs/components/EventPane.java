package bilclubs.components;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
//controllerı bu vermedim henüz diğer kartlara bakmalıyız
public class EventPane extends Pane {
    
    @FXML private Label eventName;
    @FXML private Label clubName;
    @FXML private Label gePoints;

    @FXML
    public void initialise() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/upcomingEventCard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public EventPane(String event, String club, String points) throws IOException{
        initialise();

        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                eventName.setText(event);
                clubName.setText(club);
                gePoints.setText(points);
            }
            
        });
        
    }


}

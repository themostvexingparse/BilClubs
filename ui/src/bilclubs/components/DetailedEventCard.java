package bilclubs.components;

import java.io.IOException;

import org.json.JSONObject;

import bilclubs.utils.LoadHelper;
import bilclubs.utils.RequestManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import bilclubs.controllers.Controller;

public class DetailedEventCard extends Pane{
    
    @FXML private Label name;
    @FXML private Label date;
    @FXML private Label place;
    @FXML private Label clubnamelbl;
    @FXML private ImageView eventBanner;
    @FXML private JSONObject thisEvent;

    @FXML
    public void initialise() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/detailedEventCard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public DetailedEventCard(JSONObject anEvent) throws IOException{
        initialise();

        thisEvent = anEvent;

        String eventName = anEvent.getString("name");
        String eventclub = anEvent.getString("clubName");
        String eventDate = anEvent.getString("startDate");
        String posterImage = anEvent.getString("posterImage");
        String location = anEvent.getString("location");

        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                name.setText(eventName);
                date.setText(eventDate);
                clubnamelbl.setText(eventclub);
                place.setText(location);
                Image eventImg = new Image(RequestManager.defaultAddress + posterImage, true);
                eventBanner.setImage(eventImg);

                Rectangle clip = new Rectangle();
                clip.setWidth(233);
                clip.setHeight(100);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                eventBanner.setClip(clip);


            }
        });
        
    }

    public void goToEventPage(ActionEvent e) throws IOException{
        Controller.currentEventObject = thisEvent;
        FXMLLoader eventPage = new FXMLLoader(getClass().getResource("/fxml/eventPage.fxml"));
        AnchorPane contentPane = (AnchorPane) this.getScene().lookup("#rightAnchor");
        System.out.println("contentPane: " + contentPane);
        LoadHelper.safelyLoad(eventPage, contentPane);
    }
}

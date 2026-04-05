package bilclubs.components;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class DetailedEventCard extends Pane{
    
    @FXML private Label name;
    @FXML private Label date;
    @FXML private Label place;
    @FXML private ImageView eventBanner;

    @FXML
    public void initialise() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/detailedEventCard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public DetailedEventCard(String eventname, String datestr, String placeStr) throws IOException{
        initialise();

        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                name.setText(eventname);
                date.setText(datestr);
                place.setText(placeStr);

                Rectangle clip = new Rectangle();
                clip.setWidth(233);
                clip.setHeight(100);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                eventBanner.setClip(clip);

            }
        });
        
    }
}

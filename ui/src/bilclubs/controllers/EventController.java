package bilclubs.controllers;

import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EventController {
    @FXML private Label eventNameLbl;
    @FXML private Label clubNameLbl;
    @FXML private Label descLbl;
    @FXML private Label datelbl;
    @FXML private Label placelbl;
    @FXML private Label durationlbl;
    @FXML private Label detailslbl;
    @FXML private Label gelbl;
    @FXML private ImageView eventBanner;
    @FXML private Button registerButton;

    //instance
    private JSONObject currentEvent;

    @FXML
    public void initialize(){
        currentEvent = Controller.currentEventObject;

        eventNameLbl.setText(currentEvent.getString("name"));
        clubNameLbl.setText(currentEvent.getString("clubName"));
        descLbl.setText(currentEvent.getString("description"));
        datelbl.setText(currentEvent.getString("startDate"));
        placelbl.setText(currentEvent.getString("location"));
        // durationlbl.setText(currentEvent.getString("duration"));
        // gelbl.setText(String.valueOf(currentEvent.getInt("points")));


        String posterImage = currentEvent.getString("posterImage");
        Image eventImg = new Image(RequestManager.defaultAddress + posterImage, true);
        eventBanner.setImage(eventImg);
    }


}

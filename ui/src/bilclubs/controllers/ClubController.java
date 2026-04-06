package bilclubs.controllers;

import java.io.IOException;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import bilclubs.utils.Response;
import bilclubs.components.EventPane;
import bilclubs.utils.RequestManager;

public class ClubController {
    
    @FXML private Label clubNameLbl;
    @FXML private Label clubDescLbl;
    @FXML private ImageView clubProfileImage;
    @FXML private ImageView clubBanner;
    @FXML private VBox happeningSoonVBox;
    @FXML private Label keyword1;
    @FXML private Label keyword2;
    @FXML private Label keyword3;

    @FXML
    public void initialize() throws IOException{
        JSONObject request = new JSONObject();
        request.put("action", "listClubs");
        request.put("userId", Controller.userId);
        request.put("sessionToken", Controller.sessionToken);
        Response allClubs = RequestManager.sendPostRequest("api/user", request);

        JSONArray allClubsJSONArray = allClubs.getPayload().getJSONArray("clubs");

        for(Object obj : allClubsJSONArray){
            JSONObject club = (JSONObject)obj;

            if(club.getInt("id") == Controller.currentClubId){
                clubNameLbl.setText(club.getString("clubName"));
                clubDescLbl.setText(club.getString("clubDescription").split("\n")[0]);

                //TODO: Image handling

                break;
            }
        }

        //eventleri alıcaz

        JSONObject eventRequest = new JSONObject();
        eventRequest.put("action", "getUpcomingEvents");
        eventRequest.put("clubIds", new JSONArray().put(Controller.currentClubId));
        eventRequest.put("userId", Controller.userId);
        eventRequest.put("sessionToken", Controller.sessionToken);

        Response eventResponse = RequestManager.sendPostRequest("api/user", eventRequest);
        JSONArray eventData = eventResponse.getPayload().getJSONArray("events");

        for(Object obj : eventData){
            JSONObject eventObj = (JSONObject)obj;
            EventPane event = new EventPane(eventObj.getString("name"), eventObj.getString("clubName"), ((Integer)eventObj.optInt("points", 0)).toString());
            happeningSoonVBox.getChildren().add(event);
        }
    }
    
}

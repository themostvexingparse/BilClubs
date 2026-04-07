package bilclubs.controllers;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import bilclubs.components.SearchResultPane;
import bilclubs.components.ClubPane;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;

public class BrowseController {

    @FXML
    private VBox upcomingEventsVBox;
    @FXML
    private HBox clubsYouMayLikeHBox;

    @FXML
    public void initialize() throws IOException {
        JSONObject eventReq = new JSONObject();
        eventReq.put("action", "recommend");
        eventReq.put("userId", Controller.userId);
        eventReq.put("sessionToken", Controller.sessionToken);
        eventReq.put("number", 3);

        Response eventResponse = RequestManager.sendPostRequest("api/event", eventReq);
        JSONArray userEvents = new JSONArray();
        if (eventResponse.isSuccess()) {
            userEvents = eventResponse.getPayload().optJSONArray("results");
        }
        if (userEvents == null)
            userEvents = new JSONArray();

        for (Object obj : userEvents) {
            JSONObject anEvent = (JSONObject) obj;
            SearchResultPane displayEvent = new SearchResultPane(anEvent, "Event");
            upcomingEventsVBox.getChildren().add(displayEvent);
        }

        JSONObject clubReq = new JSONObject();
        clubReq.put("action", "recommend");
        clubReq.put("userId", Controller.userId);
        clubReq.put("sessionToken", Controller.sessionToken);
        clubReq.put("number", 3);

        Response clubResponse = RequestManager.sendPostRequest("api/club", clubReq);
        JSONArray customClubs = new JSONArray();
        if (clubResponse.isSuccess()) {
            customClubs = clubResponse.getPayload().optJSONArray("results");
        }
        if (customClubs == null)
            customClubs = new JSONArray();

        for (Object obj : customClubs) {
            JSONObject aClub = (JSONObject) obj;
            ClubPane displayClub = new ClubPane(aClub.getString("name"),
                    aClub.optString("description", "").split("\n")[0], aClub.getInt("id"),
                    aClub.optString("iconFilename", ""));
            clubsYouMayLikeHBox.getChildren().add(displayClub);
        }
    }
}

package bilclubs.controllers;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import bilclubs.components.ClubPane;
import bilclubs.components.DetailedEventCard;
import bilclubs.components.EventPane;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;

public class BrowseController {
    
    @FXML private HBox upcomingEventsHBox;
    @FXML private HBox clubsYouMayLikeHBox;

    @FXML
    public void initialize() throws IOException{


        // DetailedEventCard card1 = new DetailedEventCard("fashion week", "today", "library");
        // DetailedEventCard card2 = new DetailedEventCard("fashion week", "today", "library");
        // DetailedEventCard card3 = new DetailedEventCard("fashion week", "today", "library");
        // DetailedEventCard card4 = new DetailedEventCard("fashion week", "today", "library");
        // DetailedEventCard card5 = new DetailedEventCard("fashion week", "today", "library");

        //upcomingEventsHBox.getChildren().addAll(card1, card2, card3, card4, card5);

        // ClubPane club1 = new ClubPane("acm", "acm");
        // ClubPane club2 = new ClubPane("club2", "club2");
        // ClubPane club3 = new ClubPane("club2", "club2");
        // ClubPane club4 = new ClubPane("club2", "club2");
        // ClubPane club5 = new ClubPane("club2", "club2");


        // clubsYouMayLikeHBox.getChildren().addAll(club1, club2, club3, club4, club5);


        JSONObject eventReq = new JSONObject();
        eventReq.put("action", "getUpcomingEvents");
        eventReq.put("userId", Controller.userId);
        eventReq.put("sessionToken", Controller.sessionToken);

        Response eventResponse = RequestManager.sendPostRequest("api/user", eventReq);
        JSONArray userEvents = eventResponse.getPayload().optJSONArray("events");
        if (userEvents == null) userEvents = new JSONArray();


        for (Object obj :  userEvents){
            JSONObject anEvent = (JSONObject)obj;
            
            DetailedEventCard displayEvent = new DetailedEventCard(anEvent);
            upcomingEventsHBox.getChildren().add(displayEvent);
        }

    }
}

package bilclubs.controllers;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import bilclubs.components.ClubPane;
import bilclubs.components.EventPane;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;

public class HomePageController {
    
    @FXML private HBox yourClubsHBox;
    @FXML private VBox happeningSoonVBox;
    @FXML private ImageView sleepingIcon;
    @FXML private Label noClubText;
    @FXML private Pane borderPane;


    @FXML
    public void initialize() throws IOException{
        
        // ClubPane club1 = new ClubPane("", "");
        // ClubPane club2 = new ClubPane("", "");
        // ClubPane club3 = new ClubPane("", "");
        // ClubPane club4 = new ClubPane("", "");
        // ClubPane club5 = new ClubPane("", "");
        // ClubPane club6 = new ClubPane("", "");
        // ClubPane club7 = new ClubPane("", "");

        JSONObject clubReq = new JSONObject();
        clubReq.put("userId", Controller.userId);
        clubReq.put("sessionToken", Controller.sessionToken);
        clubReq.put("targetUserId", Controller.userId);
        clubReq.put("action", "getForeignProfileClubs");

        Response clubResponse = RequestManager.sendPostRequest("api/user", clubReq);
        JSONArray clubData = clubResponse.getPayload().optJSONArray("clubs");
        if (clubData == null) clubData = new JSONArray();

        if(clubData.length() == 0){
            sleepingIcon.setVisible(true);
            noClubText.setVisible(true);
        }

        else{
            sleepingIcon.setVisible(false);
            noClubText.setVisible(false);
            borderPane.setStyle("-fx-border-color: transparent;");
        }

        JSONArray clubIds = new JSONArray();
        for(Object obj : clubData){
            JSONObject aClub = (JSONObject)obj;
            clubIds.put(aClub.getInt("id"));
        }

        if(clubIds.length() == 0)
            clubIds.put(-1);

        for(Object obj : clubData){
            JSONObject club = (JSONObject)obj;
            
            ClubPane userClubPane = new ClubPane(club.getString("name"), club.getString("description").split("\n")[0], club.getInt("id"));
            yourClubsHBox.getChildren().add(userClubPane);
        }

        JSONObject eventReq = new JSONObject();
        eventReq.put("action", "getUpcomingEvents");
        eventReq.put("clubIds", clubIds);
        eventReq.put("userId", Controller.userId);
        eventReq.put("userSpecific", false);
        eventReq.put("sessionToken", Controller.sessionToken);

        Response eventResponse = RequestManager.sendPostRequest("api/user", eventReq);
        JSONArray userEvents = eventResponse.getPayload().optJSONArray("events");
        if (userEvents == null) userEvents = new JSONArray();


        for (Object obj :  userEvents){
            JSONObject anEvent = (JSONObject)obj;
            EventPane displayEvent = new EventPane(anEvent.getString("name"), anEvent.getString("clubName"),  String.valueOf(anEvent.optInt("points", 0)));
            happeningSoonVBox.getChildren().add(displayEvent);
        }


        // EventPane event1 = new EventPane("Knitting", "Atölye Bilkent", "50");
        // EventPane event2 = new EventPane("C++ workshop", "ACM Bilkent", "30");
        // EventPane event3 = new EventPane("Knitting", "Atölye Bilkent", "50");
        // EventPane event4 = new EventPane("Knitting", "Atölye Bilkent", "50");

        // yourClubsHBox.getChildren().addAll(club1, club2, club3, club4, club5, club6, club7);
        // happeningSoonVBox.getChildren().addAll(event1, event2, event3, event4);

        //user display için json aç, getprofile action. sessiontoken userid
        //Başka profili görüntüleme: getForeignProfile sessiontoken userid targetUserId

        // JSONObject userJSON = new JSONObject();
        // userJSON.put()


    }
}

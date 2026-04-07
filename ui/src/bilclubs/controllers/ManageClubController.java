package bilclubs.controllers;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import bilclubs.components.EventCreationForm;
import bilclubs.components.EventPane;
import bilclubs.components.MemberCard;
import bilclubs.components.SearchResultPane;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class ManageClubController {
    @FXML VBox membersVBox;
    @FXML VBox clubEventsVBox;

    public void initialize() throws IOException{
        JSONObject membersJSON = new JSONObject();
        membersJSON.put("userId", Controller.userId);
        membersJSON.put("sessionToken", Controller.sessionToken);
        membersJSON.put("clubId", Controller.currentClubId);
        membersJSON.put("action", "getMembers");

        Response memberListRespone = RequestManager.sendPostRequest("api/club", membersJSON);
        JSONArray members = memberListRespone.getPayload().getJSONArray("members");

        for(Object obj : members){
            JSONObject aMember = (JSONObject)obj;

            MemberCard thisCard = new MemberCard(aMember);
            membersVBox.getChildren().add(thisCard);
        }

        JSONObject eventJSON = new JSONObject();
        eventJSON.put("action", "getUpcomingEvents");
        eventJSON.put("userId", Controller.userId);
        eventJSON.put("sessionToken", Controller.sessionToken);
        eventJSON.put("clubIds", new JSONArray().put(Controller.currentClubId));

        Response events = RequestManager.sendPostRequest("api/user", eventJSON);
        JSONArray eventList = events.getPayload().getJSONArray("events");

        for(Object obj : eventList){
            JSONObject aMember = (JSONObject)obj;

            SearchResultPane aPane = new SearchResultPane(aMember, "event");
            clubEventsVBox.getChildren().add(aPane);
        }
    }

    public void eventCreate(ActionEvent e) throws IOException{
        EventCreationForm newForm = new EventCreationForm();
    }
    
}

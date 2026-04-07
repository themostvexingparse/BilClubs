package bilclubs.controllers;

import java.io.IOException;
import java.util.ArrayList;

import javax.jdo.JDODetachedFieldAccessException;

import org.json.JSONArray;
import org.json.JSONObject;

import bilclubs.components.BanClubCard;
import bilclubs.components.BanEventCard;
import bilclubs.components.MemberCard;
import bilclubs.components.SearchResultPane;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class AdminManageController {
    @FXML VBox memberVBox;
    @FXML VBox clubVBox;
    @FXML VBox eventVBox;

    @FXML
    public void initialize() throws IOException{
        
        JSONObject request = new JSONObject();
        request.put("action", "listClubs");
        request.put("userId", Controller.userId);
        request.put("sessionToken", Controller.sessionToken);
        Response allClubs = RequestManager.sendPostRequest("api/user", request);
        JSONArray allClubsJSONArray = allClubs.getPayload().getJSONArray("clubs");

        for(Object obj : allClubsJSONArray){
            JSONObject aClub = (JSONObject)obj;
            BanClubCard newest = new BanClubCard(aClub);
            clubVBox.getChildren().add(newest);
        }

        ArrayList<Integer> allClubIds = new ArrayList<>();

        ArrayList<Integer> noDuplication = new ArrayList<>();

        for (Object obj : allClubsJSONArray) {
            JSONObject currentClub = (JSONObject) obj;
            int clubId = currentClub.getInt("id");

            JSONObject JSONmember = new JSONObject();
            JSONmember.put("action", "getMembers");
            JSONmember.put("userId", Controller.userId);
            JSONmember.put("sessionToken", Controller.sessionToken);
            JSONmember.put("clubId", clubId);

            Response memberListResponse = RequestManager.sendPostRequest("api/club", JSONmember);
            System.out.println(memberListResponse);
            JSONArray members = memberListResponse.getPayload().getJSONArray("members");

            boolean add = true;
            for (Object m : members) {
                JSONObject aMember = (JSONObject) m;
                int memberId = aMember.getInt("id");

                if (!noDuplication.contains(memberId)) {
                    noDuplication.add(memberId);
                    MemberCard thisCard = new MemberCard(aMember);
                    memberVBox.getChildren().add(thisCard);
                }
                
            }
        }

        JSONObject JSONevent = new JSONObject();
        JSONevent.put("action", "getUpcomingEvents");
        JSONevent.put("userId", Controller.userId);
        JSONevent.put("sessionToken", Controller.sessionToken);
        JSONevent.put("clubIds", new JSONArray(allClubIds));

        Response events = RequestManager.sendPostRequest("api/user", JSONevent);
        JSONArray eventList = events.getPayload().getJSONArray("events");

        for(Object obj : eventList){
            JSONObject anEvent = (JSONObject)obj;

            BanEventCard aPane = new BanEventCard(anEvent);
            eventVBox.getChildren().add(aPane);
        }


    }
}

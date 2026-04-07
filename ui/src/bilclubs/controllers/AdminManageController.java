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
import bilclubs.utils.LoadingSign;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class AdminManageController {
    @FXML VBox memberVBox;
    @FXML VBox clubVBox;
    @FXML VBox eventVBox;

    @FXML
    public void initialize() throws IOException {
        LoadingSign loadClubs = new LoadingSign();
        LoadingSign loadMembers = new LoadingSign();
        LoadingSign loadEvents = new LoadingSign();
        
        loadClubs.showLoadingIcon(clubVBox);
        loadMembers.showLoadingIcon(memberVBox);
        loadEvents.showLoadingIcon(eventVBox);

        new Thread(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "listClubs");
                request.put("userId", Controller.userId);
                request.put("sessionToken", Controller.sessionToken);
                Response allClubsResponse = RequestManager.sendPostRequest("api/user", request);
                final JSONArray allClubsJSONArray = allClubsResponse.getPayload().optJSONArray("clubs") != null ? allClubsResponse.getPayload().getJSONArray("clubs") : new JSONArray();

                ArrayList<Integer> allClubIds = new ArrayList<>();
                final JSONArray membersToRender = new JSONArray();
                ArrayList<Integer> noDuplication = new ArrayList<>();

                for (int i = 0; i < allClubsJSONArray.length(); i++) {
                    JSONObject currentClub = allClubsJSONArray.getJSONObject(i);
                    int clubId = currentClub.getInt("id");
                    allClubIds.add(clubId);

                    JSONObject JSONmember = new JSONObject();
                    JSONmember.put("action", "getMembers");
                    JSONmember.put("userId", Controller.userId);
                    JSONmember.put("sessionToken", Controller.sessionToken);
                    JSONmember.put("clubId", clubId);

                    Response memberListResponse = RequestManager.sendPostRequest("api/club", JSONmember);
                    JSONArray members = memberListResponse.getPayload().optJSONArray("members");
                    if (members != null) {
                        for (int mIndex = 0; mIndex < members.length(); mIndex++) {
                            JSONObject aMember = members.getJSONObject(mIndex);
                            int memberId = aMember.getInt("id");

                            if (!noDuplication.contains(memberId)) {
                                noDuplication.add(memberId);
                                membersToRender.put(aMember);
                            }
                        }
                    }
                }

                if (allClubIds.isEmpty()) allClubIds.add(-1);

                JSONObject JSONevent = new JSONObject();
                JSONevent.put("action", "getUpcomingEvents");
                JSONevent.put("userId", Controller.userId);
                JSONevent.put("sessionToken", Controller.sessionToken);
                JSONevent.put("clubIds", new JSONArray(allClubIds));

                Response eventsResponse = RequestManager.sendPostRequest("api/user", JSONevent);
                final JSONArray eventList = eventsResponse.getPayload().optJSONArray("events") != null ? eventsResponse.getPayload().getJSONArray("events") : new JSONArray();

                Platform.runLater(() -> {
                    try {
                        loadClubs.removeLoadingIcon(clubVBox);
                        loadMembers.removeLoadingIcon(memberVBox);
                        loadEvents.removeLoadingIcon(eventVBox);

                        for (int i = 0; i < allClubsJSONArray.length(); i++) {
                            JSONObject aClub = allClubsJSONArray.getJSONObject(i);
                            try {
                                clubVBox.getChildren().add(new BanClubCard(aClub));
                            } catch (Exception e) {}
                        }

                        for (int i = 0; i < membersToRender.length(); i++) {
                            JSONObject aMember = membersToRender.getJSONObject(i);
                            try {
                                memberVBox.getChildren().add(new MemberCard(aMember));
                            } catch (Exception e) {}
                        }

                        for (int i = 0; i < eventList.length(); i++) {
                            JSONObject anEvent = eventList.getJSONObject(i);
                            try {
                                eventVBox.getChildren().add(new BanEventCard(anEvent));
                            } catch (Exception e) {}
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
}

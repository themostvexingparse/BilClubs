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
import bilclubs.components.SearchResultPane;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import bilclubs.utils.LoadingSign;
import javafx.application.Platform;

public class HomePageController {

    @FXML
    private HBox yourClubsHBox;
    @FXML
    private VBox happeningSoonVBox;
    @FXML
    private ImageView sleepingIcon;
    @FXML
    private Label noClubText;
    @FXML
    private Pane borderPane;
    @FXML
    private ImageView sleepingIcon1;
    @FXML
    private Label noClubText1;
    @FXML
    private Pane borderPane1;

    @FXML
    public void initialize() throws IOException {

        LoadingSign loadClubs = new LoadingSign();
        LoadingSign loadEvents = new LoadingSign();
        
        loadClubs.showLoadingIcon(borderPane);
        loadEvents.showLoadingIcon(borderPane1);

        new Thread(() -> {
            try {
                JSONObject clubReq = new JSONObject();
                clubReq.put("userId", Controller.userId);
                clubReq.put("sessionToken", Controller.sessionToken);
                clubReq.put("targetUserId", Controller.userId);
                clubReq.put("action", "getForeignProfileClubs");

                Response clubResponse = RequestManager.sendPostRequest("api/user", clubReq);
                JSONArray clubData = clubResponse.getPayload().optJSONArray("clubs");
                if (clubData == null) clubData = new JSONArray();

                final JSONArray finalClubData = clubData;

                JSONArray clubIds = new JSONArray();
                for (Object obj : finalClubData) {
                    JSONObject aClub = (JSONObject) obj;
                    clubIds.put(aClub.getInt("id"));
                }
                if (clubIds.length() == 0) clubIds.put(-1);

                JSONObject eventReq = new JSONObject();
                eventReq.put("action", "getUpcomingEvents");
                eventReq.put("clubIds", clubIds);
                eventReq.put("userId", Controller.userId);
                eventReq.put("userSpecific", false);
                eventReq.put("sessionToken", Controller.sessionToken);

                Response eventResponse = RequestManager.sendPostRequest("api/user", eventReq);
                JSONArray userEvents = eventResponse.getPayload().optJSONArray("events");
                if (userEvents == null) userEvents = new JSONArray();
                
                final JSONArray finalUserEvents = userEvents;

                Platform.runLater(() -> {
                    try {
                        loadClubs.removeLoadingIcon(borderPane);
                        loadEvents.removeLoadingIcon(borderPane1);

                        if (finalClubData.length() == 0) {
                            sleepingIcon.setVisible(true);
                            noClubText.setVisible(true);
                        } else {
                            sleepingIcon.setVisible(false);
                            noClubText.setVisible(false);
                            borderPane.setStyle("-fx-border-color: transparent;");
                        }

                        for (Object obj : finalClubData) {
                            JSONObject club = (JSONObject) obj;
                            try {
                                ClubPane userClubPane = new ClubPane(club.getString("name"), club.getString("description").split("\n")[0],
                                        club.getInt("id"), club.optString("iconFilename", ""));
                                yourClubsHBox.getChildren().add(userClubPane);
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        if (finalUserEvents.length() == 0) {
                            sleepingIcon1.setVisible(true);
                            noClubText1.setVisible(true);
                        } else {
                            sleepingIcon1.setVisible(false);
                            noClubText1.setVisible(false);
                            borderPane1.setStyle("-fx-border-color: transparent;");
                        }

                        for (Object obj : finalUserEvents) {
                            JSONObject anEvent = (JSONObject) obj;
                            try {
                                SearchResultPane displayEvent = new SearchResultPane(anEvent, "Event");
                                happeningSoonVBox.getChildren().add(displayEvent);
                            } catch (Exception e) { e.printStackTrace(); }
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

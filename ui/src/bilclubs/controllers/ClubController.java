package bilclubs.controllers;

import java.io.IOException;
import java.util.Collection;

import javax.swing.Action;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import bilclubs.utils.LoadingSign;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import bilclubs.utils.Response;
import bilclubs.components.NotificationCard;
import bilclubs.components.SearchResultPane;
import bilclubs.utils.LoadHelper;
import bilclubs.utils.RequestManager;
import bilclubs.controllers.MenuController;

public class ClubController {

    @FXML
    private Label clubNameLbl;
    @FXML
    private Label clubDescLbl;
    @FXML
    private ImageView clubProfileImage;
    @FXML
    private ImageView clubBanner;
    @FXML
    private VBox happeningSoonVBox;
    @FXML
    private Label keyword1;
    @FXML
    private Label keyword2;
    @FXML
    private Label keyword3;
    @FXML
    private Button joinButton;
    @FXML
    private Button leaveButton;
    @FXML
    private Button manageButton;
    @FXML
    private Label memberCountLabel;
    @FXML
    private Label upcomingEventsLabel;

    @FXML
    public void initialize() throws IOException {
        LoadingSign loadEvents = new LoadingSign();
        loadEvents.showLoadingIcon(happeningSoonVBox);

        new Thread(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "listClubs");
                request.put("userId", Controller.userId);
                request.put("sessionToken", Controller.sessionToken);
                Response allClubs = RequestManager.sendPostRequest("api/user", request);

                final JSONArray allClubsJSONArray = allClubs.getPayload().optJSONArray("clubs") != null ? allClubs.getPayload().getJSONArray("clubs") : new JSONArray();

                JSONObject currentClubData = null;
                for (int i = 0; i < allClubsJSONArray.length(); i++) {
                    JSONObject club = allClubsJSONArray.getJSONObject(i);
                    if (club.getInt("id") == Controller.currentClubId) {
                        currentClubData = club;
                        break;
                    }
                }
                final JSONObject finalCurrentClubData = currentClubData;

                JSONObject eventRequest = new JSONObject();
                eventRequest.put("action", "getUpcomingEvents");
                eventRequest.put("clubIds", new JSONArray().put(Controller.currentClubId));
                eventRequest.put("userId", Controller.userId);
                eventRequest.put("sessionToken", Controller.sessionToken);

                Response eventResponse = RequestManager.sendPostRequest("api/user", eventRequest);
                final JSONArray eventData = eventResponse.getPayload().optJSONArray("events") != null ? eventResponse.getPayload().getJSONArray("events") : new JSONArray();

                Platform.runLater(() -> {
                    try {
                        loadEvents.removeLoadingIcon(happeningSoonVBox);
                        if (finalCurrentClubData != null) {
                            clubNameLbl.setText(finalCurrentClubData.getString("clubName"));
                            clubDescLbl.setText(finalCurrentClubData.getString("clubDescription").split("\n")[0]);
                            memberCountLabel.setText(finalCurrentClubData.getInt("memberCount") + " members");

                            int clubPrivilege = finalCurrentClubData.optInt("clubPrivilege", -1);
                            boolean isMember = clubPrivilege != -1 && clubPrivilege != 0; // 0 = not a member or banned
                            boolean isManager = clubPrivilege > 1 && isMember;  //manager / admin

                            if (isMember) {
                                joinButton.setVisible(false);
                                joinButton.setDisable(true);
                                leaveButton.setVisible(true);
                                leaveButton.setDisable(false);
                            } else {
                                leaveButton.setVisible(false);
                                leaveButton.setDisable(true);
                                joinButton.setVisible(true);
                                joinButton.setDisable(false);
                            }
                            if (isManager) {
                                manageButton.setVisible(true);
                                manageButton.setDisable(false);
                            }

                            String baseUrl = bilclubs.utils.RequestManager.defaultAddress;
                            String iconFilename = finalCurrentClubData.optString("iconFilename", "");
                            String coverFilename = finalCurrentClubData.optString("coverFilename", "");

                            if (!iconFilename.isEmpty()) {
                                Image icon = new Image(baseUrl + iconFilename, true); // true = background loading
                                clubProfileImage.setImage(icon);
                                clubProfileImage.setPreserveRatio(true);
                            }

                            if (!coverFilename.isEmpty()) {
                                Image cover = new Image(baseUrl + coverFilename, true);
                                clubBanner.setImage(cover);
                                clubBanner.setPreserveRatio(false); // stretch to fill banner area
                            }
                        }

                        if (eventData.length() == 0) {
                            upcomingEventsLabel.setText("No upcoming events for now.");
                        }

                        for (int i = 0; i < eventData.length(); i++) {
                            JSONObject eventObj = eventData.getJSONObject(i);
                            try {
                                SearchResultPane event = new SearchResultPane(eventObj, "Event");
                                happeningSoonVBox.getChildren().add(event);
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

    public void joinClub(ActionEvent e) throws IOException {
        joinButton.setDisable(true);
        new Thread(() -> {
            try {
                JSONObject joinRequest = new JSONObject();
                joinRequest.put("action", "joinClub");
                joinRequest.put("clubId", Controller.currentClubId);
                joinRequest.put("userId", Controller.userId);
                joinRequest.put("sessionToken", Controller.sessionToken);

                Response joinResponse = RequestManager.sendPostRequest("api/user", joinRequest);
                
                Platform.runLater(() -> {
                    if (joinResponse.isSuccess()) {
                        joinButton.setVisible(false);
                        joinButton.setDisable(true);
                        leaveButton.setVisible(true);
                        leaveButton.setDisable(false);

                        try {
                            NotificationCard joinNotification = new NotificationCard(NotificationCard.joinMessage, "You joined " + Controller.currentClubName);
                            AlertsController.allNotifs.add(joinNotification);
                        } catch (Exception ex) {}
                    } else {
                        joinButton.setDisable(false);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> joinButton.setDisable(false));
            }
        }).start();
    }

    public void leaveClub(ActionEvent e) throws IOException {
        leaveButton.setDisable(true);
        new Thread(() -> {
            try {
                JSONObject leaveRequest = new JSONObject();
                leaveRequest.put("action", "leaveClub");
                leaveRequest.put("clubId", Controller.currentClubId);
                leaveRequest.put("userId", Controller.userId);
                leaveRequest.put("sessionToken", Controller.sessionToken);

                Response leaveResponse = RequestManager.sendPostRequest("api/user", leaveRequest);

                Platform.runLater(() -> {
                    if (leaveResponse.isSuccess()) {
                        leaveButton.setVisible(false);
                        leaveButton.setDisable(true);

                        joinButton.setVisible(true);
                        joinButton.setDisable(false);

                        manageButton.setVisible(false);
                        manageButton.setDisable(true);

                        try {
                            NotificationCard leaveNotification = new NotificationCard(NotificationCard.leaveMessage, "Check your webmail for details.");
                            AlertsController.allNotifs.add(leaveNotification);
                        } catch (Exception ex) {}
                    } else {
                        leaveButton.setDisable(false);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> leaveButton.setDisable(false));
            }
        }).start();
    }

    public void goToManagement(ActionEvent e) throws IOException {
        FXMLLoader root = new FXMLLoader(getClass().getResource("/fxml/ClubManagementPage.fxml"));
        AnchorPane rightAnchor = (AnchorPane) manageButton.getScene().lookup("#rightAnchor");
        LoadHelper.safelyLoad(root, rightAnchor);  
    }

    // public void setParentAnchor(AnchorPane anchor) {
    //     this.parentAnchor = anchor;
    // }


    public void manageClub(ActionEvent e) throws IOException {
        // FXMLLoader manageClubPage = new FXMLLoader()
    }
}

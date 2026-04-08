package bilclubs.controllers;

import java.io.IOException;
import java.util.Collection;

import javax.swing.Action;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
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
        JSONObject request = new JSONObject();
        request.put("action", "listClubs");
        request.put("userId", Controller.userId);
        request.put("sessionToken", Controller.sessionToken);
        Response allClubs = RequestManager.sendPostRequest("api/user", request);

        JSONArray allClubsJSONArray = allClubs.getPayload().getJSONArray("clubs");

        for (Object obj : allClubsJSONArray) {
            JSONObject club = (JSONObject) obj;
            if (club.getInt("id") == Controller.currentClubId) {
                clubNameLbl.setText(club.getString("clubName"));
                clubDescLbl.setText(club.getString("clubDescription").split("\n")[0]);
                memberCountLabel.setText(club.getInt("memberCount") + " members");

                int clubPrivilege = club.optInt("clubPrivilege", -1);
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
                String iconFilename = club.optString("iconFilename", "");
                String coverFilename = club.optString("coverFilename", "");

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

                break;
            }
        }

        // eventleri alıcaz

        JSONObject eventRequest = new JSONObject();
        eventRequest.put("action", "getUpcomingEvents");
        eventRequest.put("clubIds", new JSONArray().put(Controller.currentClubId));
        eventRequest.put("userId", Controller.userId);
        eventRequest.put("sessionToken", Controller.sessionToken);

        Response eventResponse = RequestManager.sendPostRequest("api/user", eventRequest);
        JSONArray eventData = eventResponse.getPayload().getJSONArray("events");

        if (eventData.length() == 0) {
            upcomingEventsLabel.setText("No upcoming events for now.");
        }

        for (Object obj : eventData) {
            JSONObject eventObj = (JSONObject) obj;
            SearchResultPane event = new SearchResultPane(eventObj, "Event");
            happeningSoonVBox.getChildren().add(event);
        }
    }

    public void joinClub(ActionEvent e) throws IOException {
        JSONObject joinRequest = new JSONObject();
        joinRequest.put("action", "joinClub");
        joinRequest.put("clubId", Controller.currentClubId);
        joinRequest.put("userId", Controller.userId);
        joinRequest.put("sessionToken", Controller.sessionToken);

        Response joinResponse = RequestManager.sendPostRequest("api/user", joinRequest);
        System.out.println(joinResponse);

        if (joinResponse.isSuccess()) {
            joinButton.setVisible(false);
            joinButton.setDisable(true);

            leaveButton.setVisible(true);
            leaveButton.setDisable(false);

            NotificationCard joinNotification = new NotificationCard(NotificationCard.joinMessage, "Check your webmail for further details.");
            AlertsController.allNotifs.add(joinNotification);
        }
    }

    public void leaveClub(ActionEvent e) throws IOException {
        JSONObject leaveRequest = new JSONObject();
        leaveRequest.put("action", "leaveClub");
        leaveRequest.put("clubId", Controller.currentClubId);
        leaveRequest.put("userId", Controller.userId);
        leaveRequest.put("sessionToken", Controller.sessionToken);

        Response leaveResponse = RequestManager.sendPostRequest("api/user", leaveRequest);
        System.out.println(leaveResponse);

        if (leaveResponse.isSuccess()) {
            leaveButton.setVisible(false);
            leaveButton.setDisable(true);

            joinButton.setVisible(true);
            joinButton.setDisable(false);

            manageButton.setVisible(false);
            manageButton.setDisable(true);

            NotificationCard leaveNotification = new NotificationCard(NotificationCard.leaveMessage, "Check your webmail for details.");
            AlertsController.allNotifs.add(leaveNotification);

        }
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

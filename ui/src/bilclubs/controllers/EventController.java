package bilclubs.controllers;

<<<<<<< HEAD
import org.json.JSONArray;
=======
import java.io.IOException;

>>>>>>> a2b0fec (event methods added, fixes needed)
import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EventController {
<<<<<<< HEAD
=======
    @FXML private Label eventNameLbl;
    @FXML private Label clubNameLbl;
    @FXML private Label descLbl;
    @FXML private Label datelbl;
    @FXML private Label placelbl;
    @FXML private Label durationlbl;
    @FXML private Label detailslbl;
    @FXML private Label gelbl;
    @FXML private ImageView eventBanner;
    @FXML private Button registerButton;
    @FXML private Button leaveButton;

    //instance
    private JSONObject currentEvent;
>>>>>>> a2b0fec (event methods added, fixes needed)

    @FXML
    private Label eventNameLbl;
    @FXML
    private Label clubNameLbl;
    @FXML
    private Label descLbl;
    @FXML
    private Label datelbl;
    @FXML
    private Label placelbl;
    @FXML
    private Label durationlbl;
    @FXML
    private Label detailslbl;
    @FXML
    private Label gelbl;
    @FXML
    private ImageView eventBanner;
    @FXML
    private Button registerButton;
    @FXML
    private Button leaveButton;

    private JSONObject currentEvent;
    private int currentEventId;

    @FXML
    public void initialize() {
        currentEvent = Controller.currentEventObject;
        currentEventId = currentEvent.getInt("eventId");

        eventNameLbl.setText(currentEvent.getString("name"));
        clubNameLbl.setText(currentEvent.getString("clubName"));
        descLbl.setText(currentEvent.getString("description"));
        datelbl.setText(currentEvent.getString("startDate"));
        placelbl.setText(currentEvent.getString("location"));
        durationlbl.setText(currentEvent.optString("duration", "N/A"));

        int ge250 = currentEvent.optInt("GE250", 0);
        gelbl.setText(ge250 > 0 ? String.valueOf(ge250) : "None");

        int registreeCount = currentEvent.optInt("registreeCount", 0);
        Object quotaRaw = currentEvent.opt("quota");
        if (quotaRaw == null || quotaRaw == JSONObject.NULL) {
            detailslbl.setText(registreeCount + " registered  ·  Unlimited capacity");
        } else {
            int quota = currentEvent.getInt("quota");
            detailslbl.setText(registreeCount + " / " + quota + " registered");
        }

        String posterImage = currentEvent.optString("posterImage", "");
        if (!posterImage.isEmpty()) {
            Image eventImg = new Image(RequestManager.defaultAddress + posterImage, true);
            eventBanner.setImage(eventImg);
        }

        // Determine if the user is already registered for this event by
        // fetching their registered events (userSpecific=true filters to only
        // events the authenticated user has registered for).
        checkRegistrationState();
    }

<<<<<<< HEAD
    /**
     * Calls getUpcomingEvents with userSpecific=true to see if the current
     * event is already in the user's registered events. Sets button visibility
     * accordingly: show Leave if registered, show Register if not.
     */
    private void checkRegistrationState() {
        JSONObject req = new JSONObject();
        req.put("action", "getUpcomingEvents");
        req.put("userId", Controller.userId);
        req.put("sessionToken", Controller.sessionToken);
        req.put("userSpecific", true);
=======
    public void registerEvent(ActionEvent e) throws IOException {
        JSONObject registerReq = new JSONObject();
        registerReq.put("action", "register");
        registerReq.put("eventId", currentEvent.getInt("eventId"));
        registerReq.put("userId", Controller.userId);
        registerReq.put("sessionToken", Controller.sessionToken);

        Response response = RequestManager.sendPostRequest("api/event", registerReq);
        
        if (response.isSuccess()) {
            registerButton.setDisable(true);
            registerButton.setVisible(false);

            leaveButton.setVisible(true);
            leaveButton.setDisable(false);
        }
    }

    public void leaveEvent(ActionEvent e) throws IOException{
        JSONObject leaveReq = new JSONObject();
        leaveReq.put("action", "leave");
        leaveReq.put("eventId", currentEvent.getInt("eventId"));
        leaveReq.put("userId", Controller.userId);
        leaveReq.put("sessionToken", Controller.sessionToken);

        Response leaveResponse = RequestManager.sendPostRequest("api/event", leaveReq);

        if(leaveResponse.isSuccess()){
            leaveButton.setDisable(true);
            leaveButton.setVisible(false);

            registerButton.setDisable(false);
            registerButton.setVisible(true);
        }
    }

>>>>>>> a2b0fec (event methods added, fixes needed)

        Response response = RequestManager.sendPostRequest("api/user", req);
        if (!response.isSuccess())
            return;

        JSONArray events = response.getPayload().optJSONArray("events");
        if (events == null)
            return;

        boolean isRegistered = false;
        for (int i = 0; i < events.length(); i++) {
            if (events.getJSONObject(i).optInt("eventId", -1) == currentEventId) {
                isRegistered = true;
                break;
            }
        }

        setRegisteredState(isRegistered);
    }

    /** Shows the Leave button and hides Register, or vice versa. */
    private void setRegisteredState(boolean registered) {
        if (registered) {
            registerButton.setVisible(false);
            registerButton.setDisable(true);
            leaveButton.setVisible(true);
            leaveButton.setDisable(false);
        } else {
            leaveButton.setVisible(false);
            leaveButton.setDisable(true);
            registerButton.setVisible(true);
            registerButton.setDisable(false);
        }
    }

    /** Handles the Register button click. Sends action:"register" to /api/event. */
    public void registerEvent(ActionEvent e) {
        JSONObject req = new JSONObject();
        req.put("action", "register");
        req.put("eventId", currentEventId);
        req.put("userId", Controller.userId);
        req.put("sessionToken", Controller.sessionToken);

        Response response = RequestManager.sendPostRequest("api/event", req);
        System.out.println("registerEvent: " + response);

        if (response.isSuccess()) {
            setRegisteredState(true);
        }
    }

    /** Handles the Leave button click. Sends action:"leave" to /api/event. */
    public void leaveEvent(ActionEvent e) {
        JSONObject req = new JSONObject();
        req.put("action", "leave");
        req.put("eventId", currentEventId);
        req.put("userId", Controller.userId);
        req.put("sessionToken", Controller.sessionToken);

        Response response = RequestManager.sendPostRequest("api/event", req);
        System.out.println("leaveEvent: " + response);

        if (response.isSuccess()) {
            setRegisteredState(false);
        }
    }
}

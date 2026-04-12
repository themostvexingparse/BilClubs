package bilclubs.controllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class EventCreateController {
    private String name;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String place;
    private String quota;
    private String description;
    private String bannerName;

    @FXML
    TextField nameField;
    @FXML
    TextField startDateField;
    @FXML
    TextField endDateField;
    @FXML
    TextField startTimeField;
    @FXML
    TextField endTimeField;
    @FXML
    TextField placeField;
    @FXML
    TextField quotaField;
    @FXML
    TextArea descArea;

    @FXML
    Button uploadBannerButton;
    @FXML
    Button createButton;
    // instances
    private JSONObject clubJSON;

    @FXML
    public void createClubEvent(ActionEvent e) throws IOException {
        handleSubmit(e);

        if (name == null || name.isEmpty() || description == null || description.isEmpty())
            return;
        if (bannerName == null)
            return; // banner not uploaded yet

        LocalDateTime start = convert(startDate, startTime);
        LocalDateTime end = convert(endDate, endTime);

        clubJSON = new JSONObject();
        clubJSON.put("action", "create");
        clubJSON.put("userId", Controller.userId);
        clubJSON.put("clubId", Controller.currentClubId);
        clubJSON.put("sessionToken", Controller.sessionToken);
        clubJSON.put("name", name);
        clubJSON.put("description", description);
        clubJSON.put("location", place);
        clubJSON.put("posterFilename", "static/" + bannerName);
        clubJSON.put("quota", quota);
        clubJSON.put("startEpoch", start.toEpochSecond(java.time.ZoneOffset.UTC));
        clubJSON.put("endEpoch", end.toEpochSecond(java.time.ZoneOffset.UTC));

        Response createEvent = RequestManager.sendPostRequest("api/event", clubJSON);

        if (!createEvent.isSuccess()) {
            System.out.println(createEvent.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Event could not be created.");
            alert.setHeaderText(null);
            alert.setContentText(createEvent.toString());
            alert.showAndWait();
        }

        ((Stage) nameField.getScene().getWindow()).close(); // stays here since there's no success/fail label
    }

    public void handleSubmit(ActionEvent e) {
        String name = nameField.getText().trim();
        this.name = name;

        if (name.isEmpty()) {
            nameField.requestFocus();
            return;
        }
        String startDate = startDateField.getText().trim();
        this.startDate = startDate;

        if (startDate.isEmpty()) {
            startDateField.requestFocus();
            return;
        }
        String endDate = endDateField.getText().trim();
        this.endDate = endDate;

        if (endDate.isEmpty()) {
            endDateField.requestFocus();
            return;
        }
        String startTime = startTimeField.getText().trim();
        this.startTime = startTime;

        if (startTime.isEmpty()) {
            startTimeField.requestFocus();
            return;
        }
        String endTime = endTimeField.getText().trim();
        this.endTime = endTime;

        if (endTime.isEmpty()) {
            endTimeField.requestFocus();
            return;
        }
        String place = placeField.getText().trim();
        this.place = place;

        if (place.isEmpty()) {
            placeField.requestFocus();
            return;
        }
        String quota = quotaField.getText().trim();
        this.quota = quota;

        if (quota.isEmpty()) {
            quotaField.requestFocus();
            return;
        }

        String description = descArea.getText();
        this.description = description;
        if (description.isEmpty()) {
            descArea.requestFocus();
            return;
        }

    }

    @FXML
    public void uploadBanner(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Event Banner");
        Stage stage = (Stage) nameField.getScene().getWindow(); // fixed cast
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null)
            return; // null check

        JSONObject auth = new JSONObject();
        auth.put("action", "upload");
        auth.put("userId", Controller.userId);
        auth.put("sessionToken", Controller.sessionToken);

        Response uploadResponse = RequestManager.uploadFile(auth, selectedFile);
        this.bannerName = uploadResponse.getPayload().getJSONObject("fileMap").getString(selectedFile.getName());
        System.out.println("Banner uploaded: " + bannerName);
        uploadBannerButton.setText("Re-upload");
    }

    public LocalDateTime convert(String date, String time) {
        String dateTime = date + " " + time;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateandTime = LocalDateTime.parse(dateTime, formatter);
        return dateandTime;
    }

}

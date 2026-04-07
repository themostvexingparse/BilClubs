package bilclubs.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import bilclubs.components.ClubDisplay;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;


public class ProfileController {
    @FXML private ImageView profileImage;
    @FXML private VBox ClubsBox;
    @FXML private VBox ClubsBox1;
    @FXML private Label namelbl;
    @FXML private Label deptlbl;
    @FXML private Label privlbl;

    @FXML
    public void initialize() throws IOException{
        double imageSize = 150; 
        Circle clipCircle = new Circle(profileImage.getFitWidth()/2, profileImage.getFitHeight()/2, imageSize/2);
        profileImage.setClip(clipCircle);

        JSONObject request = new JSONObject();
        request.put("userId", Controller.userId);
        request.put("sessionToken", Controller.sessionToken);
        request.put("action", "getProfile");
        Response userProfile = RequestManager.sendPostRequest("api/user", request);

        JSONObject userData = userProfile.getPayload();

        Image image = new Image(RequestManager.defaultAddress + userData.getString("profilePicture"), true);
        System.out.println(RequestManager.defaultAddress + userData.getString("profilePicture"));
        image.errorProperty().addListener((obs, oldVal, isError) -> {
            if (isError) {
                System.out.println("Image failed to load: " + image.getException().getMessage());
            }
        });
        image.progressProperty().addListener((obs, oldVal, progress) -> {
            if (progress.doubleValue() == 1.0 && !image.isError()) {
                profileImage.setImage(image);
                System.out.println("Image loaded successfully");
            }
        });
        profileImage.setImage(image);

        namelbl.setText(userData.getString("firstName") + " " + userData.getString("lastName"));
        deptlbl.setText("Department: " + userData.getString("major"));

        JSONObject clubRequest = new JSONObject();
        clubRequest.put("userId", Controller.userId);
        clubRequest.put("sessionToken", Controller.sessionToken);
        clubRequest.put("targetUserId", Controller.userId);
        clubRequest.put("action", "getForeignProfileClubs");
        Response userClubs = RequestManager.sendPostRequest("api/user", clubRequest);

        JSONArray clubData = userClubs.getPayload().getJSONArray("clubs");

        for(Object obj : clubData){
            JSONObject club = (JSONObject)obj;
            
            ClubDisplay userClub = new ClubDisplay();
            userClub.setName(club.getString("name"));
            userClub.setIcon(club.optString("iconFilename", ""));

            ClubsBox.getChildren().add(userClub);

        }

        // Populate interests
        JSONArray interests = userData.optJSONArray("interests");
        if (interests != null) {
            for (int i = 0; i < interests.length(); i++) {
                String interest = interests.optString(i, "").trim();
                if (interest.isEmpty() || interest.toLowerCase().startsWith("biography:")) {
                    continue;
                }

                Pane card = new Pane();
                card.setPrefSize(250, 50);
                card.setMaxSize(250, 50);
                card.setMinSize(250, 50);
                card.setStyle("-fx-background-radius: 12; -fx-background-color: -menu-blue;");
                card.getStyleClass().add("club-card");

                HBox content = new HBox();
                content.setAlignment(Pos.CENTER_LEFT);
                content.setSpacing(10);
                content.setPrefSize(250, 50);
                content.setPadding(new Insets(5, 12, 5, 12));

                Label interestLabel = new Label(interest);
                interestLabel.setFont(Font.font("Arial", 12));
                interestLabel.setWrapText(true);
                interestLabel.setMaxWidth(220);

                content.getChildren().add(interestLabel);
                card.getChildren().add(content);

                DropShadow shadow = new DropShadow();
                shadow.setHeight(10);
                shadow.setRadius(4.5);
                shadow.setWidth(10);
                card.setEffect(shadow);

                ClubsBox1.getChildren().add(card);
            }
        }

    }

    public void uploadProfilePicture(MouseEvent e) throws IOException{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your Profile");
        Stage stage = (Stage)profileImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        JSONObject auth = new JSONObject();
        auth.put("action", "upload");
        auth.put("userId", Controller.userId);
        auth.put("sessionToken", Controller.sessionToken);

        if (selectedFile != null) {
            Response profile = RequestManager.uploadFile(auth, selectedFile);
            String file = profile.getPayload().getJSONObject("fileMap").getString(selectedFile.getName());
            System.out.println(profile.getPayload());
            System.out.println(selectedFile.getName());

            Image image = new Image(RequestManager.defaultAddress + "static/" + file, true);
            image.errorProperty().addListener((obs, oldVal, isError) -> {
                if (isError) {
                    System.out.println("Image failed to load: " + image.getException().getMessage());
                }
            });
            image.progressProperty().addListener((obs, oldVal, progress) -> {
                if (progress.doubleValue() == 1.0 && !image.isError()) {
                    profileImage.setImage(image);
                    System.out.println("Image loaded successfully");
                }
            });
            System.out.println(RequestManager.defaultAddress + "static/" + file);
            profileImage.setImage(image);
            JSONObject pfpreq = new JSONObject();
            pfpreq.put("action", "updateProfile");
            pfpreq.put("userId", Controller.userId);
            pfpreq.put("sessionToken", Controller.sessionToken);
            pfpreq.put("profilePicture", file);
            RequestManager.sendPostRequest("api/user", pfpreq);
        }
    }

    

}

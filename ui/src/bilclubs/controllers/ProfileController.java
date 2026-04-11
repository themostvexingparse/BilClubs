package bilclubs.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import bilclubs.components.ClubDisplay;
import bilclubs.utils.LoadHelper;
import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;


public class ProfileController {
    @FXML private ImageView profileImage;
    @FXML private VBox ClubsBox;
    @FXML private Label namelbl;
    @FXML private Label deptlbl;
    @FXML private Label privlbl;
    @FXML private Button adminButton;

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

        int privilege = userData.getInt("privilege");

        // if(privilege & 1 == 0){
            
        // }

        if((privilege & 15) == 15){
            adminButton.setDisable(false);
            adminButton.setVisible(true);
        }

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
            // userClub.setDesc(club.getString("description").split("\n")[0]);

            ClubsBox.getChildren().add(userClub);

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

    public void goToManage(ActionEvent e) throws IOException{
        FXMLLoader manageRoot = new FXMLLoader(getClass().getResource("/fxml/adminManagePage.fxml"));
        AnchorPane contentPane = (AnchorPane) namelbl.getScene().lookup("#rightAnchor");
        LoadHelper.safelyLoad(manageRoot, contentPane);
    }

    

}

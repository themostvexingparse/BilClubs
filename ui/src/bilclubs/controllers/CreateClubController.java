package bilclubs.controllers;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class CreateClubController {
    private String name;
    private String desc;
    private String iconName;
    private String bannerName;
    @FXML TextField nameField;
    @FXML TextArea descArea;
    @FXML Button uploadIconButton;
    @FXML Button uploadBannerButton;
    @FXML Button createButton;
    @FXML Label errorLabel;
    //instances
    private JSONObject clubJSON;

    @FXML
    public void createClub(ActionEvent e){
        handleSubmit(e);
        try {
            uploadClubIcon(e);
            uploadBanner(e);
        } catch (Exception a) {
            a.printStackTrace();
        }
        clubJSON=new JSONObject();
        clubJSON.put("action", "create");
        clubJSON.put("userId", Controller.userId);
        clubJSON.put("clubId", 100);
        clubJSON.put("sessionToken", Controller.sessionToken);
        clubJSON.put("clubName", name);
        clubJSON.put("clubDescription",desc);
        clubJSON.put("iconFilename",iconName);
        clubJSON.put("coverFilename",bannerName);
        Response createClub=RequestManager.sendPostRequest("api/club",clubJSON);
        System.out.println(createClub.getPayload().toString());
        System.out.println(createClub.getErrorMessage());
        System.out.println(name);
        if(!createClub.isSuccess()){
            errorLabel.setText("Please Enter Right Information!");
            errorLabel.setVisible(true);
        }
        else{
             errorLabel.setText("Club created");
            errorLabel.setVisible(true);
        }
    }

    public void handleSubmit(ActionEvent e) {
        String name = nameField.getText().trim();
        this.name=name;

        if (name.isEmpty()) { 
            nameField.requestFocus(); 
            return; 
        }

        String desc = descArea.getText();
        this.desc=desc;
        if(desc.isEmpty()){
            descArea.requestFocus();
            return;
        }
    
        ((Stage) nameField.getScene().getWindow()).close();
    }

    public void uploadClubIcon(ActionEvent e) throws IOException{

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your Profile");
        Stage stage = (Stage)((Stage) nameField.getScene().getWindow());
        File selectedFile = fileChooser.showOpenDialog(stage);
        String fileName=selectedFile.getName();
        this.iconName=fileName;

        uploadIconButton.setText("Re-upload");
    }

    public void uploadBanner(ActionEvent e) throws IOException{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your Profile");
        Stage stage = (Stage)((Stage) nameField.getScene().getWindow());
        File selectedFile = fileChooser.showOpenDialog(stage);
        String fileName=selectedFile.getName();
        this.bannerName=fileName;

        uploadBannerButton.setText("Re-upload");
    }


}

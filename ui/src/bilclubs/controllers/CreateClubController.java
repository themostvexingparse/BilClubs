package bilclubs.controllers;

import java.io.File;
import java.io.IOException;

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

public class CreateClubController {
    @FXML TextField nameField;
    @FXML TextArea descArea;
    @FXML Button uploadIconButton;
    @FXML Button uploadBannerButton;
    //instances
    private JSONObject clubJSON;

    public void handleSubmit() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) { 
            nameField.requestFocus(); 
            return; 
        }

        String desc = descArea.getText();
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

        uploadIconButton.setText("Re-upload");

    }

    public void uploadBanner(ActionEvent e) throws IOException{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your Profile");
        Stage stage = (Stage)((Stage) nameField.getScene().getWindow());
        File selectedFile = fileChooser.showOpenDialog(stage);

        uploadBannerButton.setText("Re-upload");
        
    }


}

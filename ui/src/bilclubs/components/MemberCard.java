package bilclubs.components;

import java.io.IOException;

import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class MemberCard extends Pane {
    
    @FXML Label namelbl;
    @FXML ImageView pfp;

    @FXML 
    public void initialise() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/memberCard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public MemberCard(JSONObject memberObject) throws IOException{
        initialise();

        String name = memberObject.getString("name");
        namelbl.setText(name);

        String profilePicture = memberObject.optString("profilePicture", "");
        Image profileImage = new Image(RequestManager.defaultAddress + profilePicture, true);
        pfp.setImage(profileImage);
        
    }
}

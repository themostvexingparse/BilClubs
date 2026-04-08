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

public class BanClubCard extends Pane{
    @FXML Label clublbl;
    @FXML ImageView pfp;


    @FXML 
    public void initialise() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/banclubcard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public BanClubCard(JSONObject eventObject) throws IOException{
        initialise();

        String name = eventObject.getString("clubName");
        clublbl.setText(name);

        String profilePicture = eventObject.optString("iconFileName", "");
        Image profileImage = new Image(RequestManager.defaultAddress + profilePicture, true);
        pfp.setImage(profileImage);
        
    }
}

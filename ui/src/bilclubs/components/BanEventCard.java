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

public class BanEventCard extends Pane{
    @FXML Label eventlbl;
    @FXML Label clublbl;
    @FXML ImageView pfp;


    @FXML 
    public void initialise() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/baneventcard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public BanEventCard(JSONObject eventObject) throws IOException{
        initialise();

        String name = eventObject.getString("name");
        eventlbl.setText(name);

        String profilePicture = eventObject.optString("posterImage", "");
        Image profileImage = new Image(RequestManager.defaultAddress + profilePicture, true);
        pfp.setImage(profileImage);
        
    }
}

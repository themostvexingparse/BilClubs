package bilclubs.components;

import java.io.IOException;

import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import bilclubs.controllers.Controller;

public class BanEventCard extends Pane{
    @FXML Label eventlbl;
    @FXML Label clublbl;
    @FXML ImageView pfp;
    @FXML Button banBtn;


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
        
        banBtn.setOnAction(e -> {
            banBtn.setDisable(true);
            new Thread(() -> {
                try {
                    JSONObject request = new JSONObject();
                    request.put("action", "banEvent");
                    request.put("userId", Controller.userId);
                    request.put("sessionToken", Controller.sessionToken);
                    int eventId = eventObject.has("eventId") ? eventObject.getInt("eventId") : eventObject.getInt("id");
                    request.put("eventId", eventId);
                    
                    Response res = RequestManager.sendPostRequest("api/user", request);
                    if (res.getCode() == 200) {
                        Platform.runLater(() -> {
                            if (this.getParent() instanceof Pane) {
                                ((Pane) this.getParent()).getChildren().remove(this);
                            }
                        });
                    } else {
                        Platform.runLater(() -> banBtn.setDisable(false));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> banBtn.setDisable(false));
                }
            }).start();
        });
    }
}

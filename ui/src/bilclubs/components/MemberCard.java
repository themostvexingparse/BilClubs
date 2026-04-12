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

public class MemberCard extends Pane {

    @FXML
    Label namelbl;
    @FXML
    Label majorlbl;
    @FXML
    ImageView pfp;
    @FXML
    Button banBtn;

    @FXML
    public void initialise() throws IOException {
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/memberCard.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public MemberCard(JSONObject memberObject) throws IOException {
        initialise();

        String name = memberObject.getString("name");
        namelbl.setText(name);

        String major = memberObject.optString("major", "Unspecified Major");
        if (majorlbl != null) {
            majorlbl.setText(major);
        }

        String profilePicture = memberObject.optString("profilePicture", "");
        Image profileImage = new Image(RequestManager.defaultAddress + profilePicture, true);
        pfp.setImage(profileImage);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(pfp.getFitWidth(), pfp.getFitHeight());
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        pfp.setClip(clip);

        banBtn.setOnAction(e -> {
            banBtn.setDisable(true);
            new Thread(() -> {
                try {
                    JSONObject request = new JSONObject();
                    request.put("action", "banUser");
                    request.put("userId", Controller.userId);
                    request.put("sessionToken", Controller.sessionToken);
                    request.put("targetUserId", memberObject.getInt("id"));

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

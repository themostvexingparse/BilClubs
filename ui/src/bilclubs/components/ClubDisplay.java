package bilclubs.components;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;

import bilclubs.utils.RequestManager;

public class ClubDisplay extends Pane {
    
    @FXML private Rectangle clubIcon;
    @FXML private Label namelbl;

    //instances
    private String name;

    public ClubDisplay() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/userClubPane.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();

        // Set default club icon
        Image defaultImg = new Image(getClass().getResourceAsStream("/assets/default-club-icon.png"));
        clubIcon.setFill(new ImagePattern(defaultImg));
    }

    public void setName(String aName){
        this.name = aName;
        namelbl.setText(name);
    }

    public void setIcon(String iconFilename) {
        if (iconFilename != null && !iconFilename.isEmpty() && !iconFilename.contains("default")) {
            Image icon = new Image(RequestManager.defaultAddress + iconFilename, true);
            icon.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 && !icon.isError()) {
                    clubIcon.setFill(new ImagePattern(icon));
                }
            });
            if (icon.getProgress() >= 1.0 && !icon.isError()) {
                clubIcon.setFill(new ImagePattern(icon));
            }
        }
    }

}

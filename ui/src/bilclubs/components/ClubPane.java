package bilclubs.components;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import bilclubs.utils.LoadHelper;
import bilclubs.utils.RequestManager;
import bilclubs.controllers.Controller;

public class ClubPane extends Pane {

    @FXML
    Rectangle clubPic;
    @FXML
    Label nameLabel;
    @FXML
    Label descLabel;

    // instances
    private String name;
    private String desc;
    private Stage stage;
    private Integer clubId;

    public ClubPane(String clubname, String clubDesc, Integer clubIdVal) throws IOException {
        this(clubname, clubDesc, clubIdVal, "");
    }

    public ClubPane(String clubname, String clubDesc, Integer clubIdVal, String iconFilename) throws IOException {
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/clubDisplayerPane.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();

        nameLabel.setText(clubname);
        descLabel.setText(clubDesc);

        this.name = clubname;
        this.desc = clubDesc;
        this.clubId = clubIdVal;

        // Load club icon from the server, fall back to default
        Image defaultImg = new Image(getClass().getResourceAsStream("/assets/bilclubs logo 1.png"));
        clubPic.setFill(new ImagePattern(defaultImg));

        if (iconFilename != null && !iconFilename.isEmpty() && !iconFilename.contains("default")) {
            Image icon = new Image(RequestManager.defaultAddress + iconFilename, true);
            icon.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 && !icon.isError()) {
                    clubPic.setFill(new ImagePattern(icon));
                }
            });
            if (icon.getProgress() >= 1.0 && !icon.isError()) {
                clubPic.setFill(new ImagePattern(icon));
            }
        }
    }

    public void setName(String aName) {
        this.name = aName;
        nameLabel.setText(name);
    }

    public void setDesc(String aDesc) {
        this.desc = aDesc;
        descLabel.setText(desc);
    }

    public void goToClub(ActionEvent e) throws IOException {
        Controller.currentClubId = this.clubId;
        System.out.println("Navigating to club: " + this.clubId);
        FXMLLoader clubPage = new FXMLLoader(getClass().getResource("/fxml/ClubPage.fxml"));
        AnchorPane contentPane = (AnchorPane) this.getScene().lookup("#rightAnchor");
        System.out.println("contentPane: " + contentPane);
        LoadHelper.safelyLoad(clubPage, contentPane);
        System.out.println("Load called");
    }

}

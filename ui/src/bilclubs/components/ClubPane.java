package bilclubs.components;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import bilclubs.utils.LoadHelper;

public class ClubPane extends Pane {

    @FXML ImageView clubPic;
    @FXML Label nameLabel;
    @FXML Label descLabel;

    //instances
    private String name;
    private String desc;
    private Stage stage;

    public ClubPane(String clubname, String clubDesc) throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/clubDisplayerPane.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();

        nameLabel.setText(clubname);
        descLabel.setText(clubDesc);

        this.name = clubname;
        this.desc = clubDesc;
    }

    public void setName(String aName){
        this.name = aName;
        nameLabel.setText(name);
    }

    public void setDesc(String aDesc){
        this.desc = aDesc;
        descLabel.setText(desc);
    }

    public void goToClub(ActionEvent e) throws IOException{
        FXMLLoader clubPage = new FXMLLoader(getClass().getResource("/fxml/ClubPage.fxml"));
        AnchorPane contentPane = (AnchorPane) this.getScene().lookup("#rightAnchor");
        LoadHelper.safelyLoad(clubPage, contentPane);
    }
    
}

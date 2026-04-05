package bilclubs.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import bilclubs.components.ClubPane;
import bilclubs.components.EventPane;

public class HomePageController {
    
    @FXML private HBox yourClubsHBox;
    @FXML private VBox happeningSoonVBox;

    @FXML
    public void initialize() throws IOException{
        
        ClubPane club1 = new ClubPane("", "");
        ClubPane club2 = new ClubPane("", "");
        ClubPane club3 = new ClubPane("", "");
        ClubPane club4 = new ClubPane("", "");
        ClubPane club5 = new ClubPane("", "");
        ClubPane club6 = new ClubPane("", "");
        ClubPane club7 = new ClubPane("", "");

        EventPane event1 = new EventPane("Knitting", "Atölye Bilkent", "50");
        EventPane event2 = new EventPane("C++ workshop", "ACM Bilkent", "30");
        EventPane event3 = new EventPane("Knitting", "Atölye Bilkent", "50");
        EventPane event4 = new EventPane("Knitting", "Atölye Bilkent", "50");

        yourClubsHBox.getChildren().addAll(club1, club2, club3, club4, club5, club6, club7);
        happeningSoonVBox.getChildren().addAll(event1, event2, event3, event4);

        //user display için json aç, getprofile action. sessiontoken userid
        //Başka profili görüntüleme: getForeignProfile sessiontoken userid targetUserId

        // JSONObject userJSON = new JSONObject();
        // userJSON.put()


    }
}

package bilclubs.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Parent;
import javafx.stage.*;
import javafx.scene.control.*;

import java.io.IOException;

import bilclubs.utils.LoadHelper;


public class MenuController {
    private Stage stage;

    @FXML Button home;
    @FXML Button browse;
    @FXML Button calendar;
    @FXML Button alerts;
    @FXML Button profile;
    @FXML Button settings;
    @FXML private AnchorPane rightAnchor;
    @FXML
    private Button signOutButton;


    //instances
    Parent homeRoot;
    Parent browseRoot;
    Parent calendarRoot;
    Parent alertsRoot;
    Parent profileRoot;
    Parent settingsRoot;
    FXMLLoader welcomeRoot;

    @FXML
    public void initialize() throws IOException {
        homeRoot = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
        browseRoot = FXMLLoader.load(getClass().getResource("/fxml/Browse.fxml"));
        calendarRoot = FXMLLoader.load(getClass().getResource("/fxml/Calendar.fxml"));
        alertsRoot = FXMLLoader.load(getClass().getResource("/fxml/Alerts.fxml"));
        profileRoot = FXMLLoader.load(getClass().getResource("/fxml/adminProfile.fxml"));
        settingsRoot = FXMLLoader.load(getClass().getResource("/fxml/Settings.fxml"));
        welcomeRoot = new FXMLLoader(getClass().getResource("/fxml/welcomescenebuilder.fxml"));

        rightAnchor.getChildren().setAll(homeRoot);

    }


    public void goToSettings(ActionEvent e) throws IOException{
        rightAnchor.getChildren().setAll(settingsRoot);
        
    }

    public void goToAdminProfile(ActionEvent e) throws IOException{
        rightAnchor.getChildren().setAll(profileRoot);
        
    }

    public void goToBrowse(ActionEvent e) throws IOException{
        rightAnchor.getChildren().setAll(browseRoot);
        
    }

    public void goToCalendar(ActionEvent e) throws IOException{
        rightAnchor.getChildren().setAll(calendarRoot);
        
    }

    public void goToAlerts(ActionEvent e) throws IOException{
        rightAnchor.getChildren().setAll(alertsRoot);
        
    }

    public void goToHome(ActionEvent e) throws IOException{
        rightAnchor.getChildren().setAll(homeRoot);
        
    }

    //sesssiontoken userid
    //signouta basınca welcomea geri at ikisini de nulla

    public void signOut(ActionEvent e) throws IOException{
        stage = (Stage) home.getScene().getWindow();

        Controller.sessionToken = null;
        Controller.userId = null;

        LoadHelper.safelyLoad(welcomeRoot, stage);

    }

    public AnchorPane getRightAnchor(){
        return this.rightAnchor;
    }

}

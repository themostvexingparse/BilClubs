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


    @FXML
    public void initialize() throws IOException {
        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/Home.fxml")), rightAnchor);
    }


    public void goToHome(ActionEvent e) throws IOException {
        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/Home.fxml")), rightAnchor);
    }

    public void goToBrowse(ActionEvent e) throws IOException {
        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/Browse.fxml")), rightAnchor);
    }

    public void goToCalendar(ActionEvent e) throws IOException {
        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/Calendar.fxml")), rightAnchor);
    }

    public void goToAlerts(ActionEvent e) throws IOException {
        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/Alerts.fxml")), rightAnchor);
    }

    public void goToAdminProfile(ActionEvent e) throws IOException {
        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/adminProfile.fxml")), rightAnchor);
    }

    public void goToSettings(ActionEvent e) throws IOException {
        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/Settings.fxml")), rightAnchor);
    }

    //sesssiontoken userid
    //signouta basınca welcomea geri at ikisini de nulla

    public void signOut(ActionEvent e) throws IOException{
        stage = (Stage) home.getScene().getWindow();

        Controller.sessionToken = null;
        Controller.userId = null;

        LoadHelper.safelyLoad(new FXMLLoader(getClass().getResource("/fxml/welcomescenebuilder.fxml")), stage);

    }

    public AnchorPane getRightAnchor(){
        return this.rightAnchor;
    }

}

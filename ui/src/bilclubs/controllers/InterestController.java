package bilclubs.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.*;

import bilclubs.utils.LoadHelper;

public class InterestController {

    private Stage stage;
    private Stage popupModeStage = null;

    public void setPopupMode(Stage s) {
        this.popupModeStage = s;
    }

    public void setPreloadedInterests(org.json.JSONArray arr) {
        if (arr == null) return;
        List<String> userInterests = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) userInterests.add(arr.optString(i));

        for (Node node : interestAnchor.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox check = (CheckBox) node;
                if (userInterests.contains(check.getText())) {
                    check.setSelected(true);
                }
            }
        }
    }

    public void setPreloadedBio(String bio) {
        if (bioArea != null) {
            bioArea.setText(bio);
        }
    }

    @FXML
    private CheckBox archeology;
    @FXML
    private CheckBox astronomy;
    @FXML
    private CheckBox art;
    @FXML
    private CheckBox coding;
    @FXML
    private CheckBox scifi;
    @FXML
    private CheckBox plants;
    @FXML
    private CheckBox language;
    @FXML
    private CheckBox environment;
    @FXML
    private CheckBox animation;
    @FXML
    private CheckBox dance;
    @FXML
    private CheckBox games;
    @FXML
    private CheckBox counseling;
    @FXML
    private CheckBox sports;
    @FXML
    private CheckBox literature;
    @FXML
    private CheckBox leadership;
    @FXML
    private CheckBox space;
    @FXML
    private CheckBox culture;
    @FXML
    private CheckBox chemistry;
    @FXML
    private CheckBox economics;
    @FXML
    private CheckBox management;
    @FXML
    private CheckBox innovation;
    @FXML
    private CheckBox cooking;
    @FXML
    private CheckBox debate;
    @FXML
    private CheckBox music;
    @FXML
    private CheckBox knitting;
    @FXML
    private CheckBox fashion;
    @FXML
    private CheckBox media;
    @FXML
    private CheckBox engineering;
    @FXML
    private CheckBox history;
    @FXML
    private CheckBox psychology;
    @FXML
    private CheckBox genetics;
    @FXML
    private CheckBox animals;
    @FXML
    private CheckBox law;
    @FXML
    private CheckBox philosophy;
    @FXML
    private CheckBox physics;
    @FXML
    private CheckBox investment;
    @FXML
    private CheckBox photography;
    @FXML
    private CheckBox journalism;
    @FXML
    private CheckBox politics;
    @FXML
    private TextArea bioArea;
    @FXML
    private Button submitButton;

    // anchor
    @FXML
    private FlowPane interestAnchor;

    // list
    private static ArrayList<String> interestList = new ArrayList<>();

    public void getInterests(ActionEvent e) throws IOException {

        for (Node node : interestAnchor.getChildren()) {

            CheckBox check;
            if (node instanceof CheckBox) {
                check = (CheckBox) node;
            } else
                continue;
            if (check.isSelected()) {
                interestList.add(check.getText());
                System.out.println(check.getText()); // problem yok
            }
        }

        if (popupModeStage != null) {
            try {
                org.json.JSONObject req = new org.json.JSONObject();
                req.put("action", "setInterests");
                req.put("userId", Controller.userId);
                req.put("sessionToken", Controller.sessionToken);
                req.put("interests", new org.json.JSONArray(interestList));
                bilclubs.utils.RequestManager.sendPostRequest("api/user", req);

                org.json.JSONObject embedReq = new org.json.JSONObject();
                embedReq.put("action", "generateEmbeddings");
                embedReq.put("userId", Controller.userId);
                embedReq.put("sessionToken", Controller.sessionToken);
                bilclubs.utils.RequestManager.sendPostRequest("api/user", embedReq);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            interestList.clear();
            popupModeStage.close();
            return;
        }

        FXMLLoader mainPageFXML = new FXMLLoader(getClass().getResource("/fxml/interestKeywords.fxml"));
        stage = (Stage) archeology.getScene().getWindow();

        LoadHelper.safelyLoad(mainPageFXML, stage);

    }

    public void goToHomePage(ActionEvent e) throws IOException {
        try {
            String bio = bioArea.getText();
            if (bio != null) {
                org.json.JSONObject bioReq = new org.json.JSONObject();
                bioReq.put("action", "updateProfile");
                bioReq.put("userId", Controller.userId);
                bioReq.put("sessionToken", Controller.sessionToken);
                bioReq.put("biography", bio.trim());
                bilclubs.utils.RequestManager.sendPostRequest("api/user", bioReq);
            }
            
            if (popupModeStage == null) {
                org.json.JSONObject req = new org.json.JSONObject();
                req.put("action", "setInterests");
                req.put("userId", Controller.userId);
                req.put("sessionToken", Controller.sessionToken);
                req.put("interests", new org.json.JSONArray(interestList));
                bilclubs.utils.RequestManager.sendPostRequest("api/user", req);
            }

            org.json.JSONObject embedReq = new org.json.JSONObject();
            embedReq.put("action", "generateEmbeddings");
            embedReq.put("userId", Controller.userId);
            embedReq.put("sessionToken", Controller.sessionToken);
            bilclubs.utils.RequestManager.sendPostRequest("api/user", embedReq);

            if (popupModeStage == null) {
                interestList.clear();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (popupModeStage != null) {
            popupModeStage.close();
            return;
        }

        stage = (Stage) submitButton.getScene().getWindow();
        FXMLLoader mainPageFXML = new FXMLLoader(getClass().getResource("/fxml/welcomescenebuilder.fxml"));
        LoadHelper.safelyLoad(mainPageFXML, stage);
    }

}

package bilclubs.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
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

    @FXML private CheckBox archeology;
    @FXML private CheckBox astronomy;
    @FXML private CheckBox art;
    @FXML private CheckBox coding;
    @FXML private CheckBox scifi;
    @FXML private CheckBox plants;
    @FXML private CheckBox language;
    @FXML private CheckBox environment;
    @FXML private CheckBox animation;
    @FXML private CheckBox dance;
    @FXML private CheckBox games;
    @FXML private CheckBox counseling;
    @FXML private CheckBox sports;
    @FXML private CheckBox literature;
    @FXML private CheckBox leadership;
    @FXML private CheckBox space;
    @FXML private CheckBox culture;
    @FXML private CheckBox chemistry;
    @FXML private CheckBox economics;
    @FXML private CheckBox management;
    @FXML private CheckBox innovation;
    @FXML private CheckBox cooking;
    @FXML private CheckBox debate;
    @FXML private CheckBox music;
    @FXML private CheckBox knitting;
    @FXML private CheckBox fashion;
    @FXML private CheckBox media;
    @FXML private CheckBox engineering;
    @FXML private CheckBox history;
    @FXML private CheckBox psychology;
    @FXML private CheckBox genetics;
    @FXML private CheckBox animals;
    @FXML private CheckBox law;
    @FXML private CheckBox philosophy;
    @FXML private CheckBox physics;
    @FXML private CheckBox investment;
    @FXML private CheckBox photography;
    @FXML private CheckBox journalism;
    @FXML private CheckBox politics;
    @FXML private TextArea bioArea;

    //anchor
    @FXML private AnchorPane interestAnchor;

    //list
    private ArrayList<String> interestList = new ArrayList<>();

    public void getInterests(ActionEvent e) throws IOException{

        for (Node node : interestAnchor.getChildren()){

            CheckBox check;
            if (node instanceof CheckBox) {
                check = (CheckBox) node;
            } else continue;
            if (check.isSelected()){
                interestList.add(check.getText());
                System.out.println(check.getText()); //problem yok
            }
        }

        FXMLLoader mainPageFXML = new FXMLLoader(getClass().getResource("/fxml/interestKeywords.fxml"));
        stage = (Stage)archeology.getScene().getWindow();
        
        LoadHelper.safelyLoad(mainPageFXML, stage);

    }

    public void goToHomePage(ActionEvent e) throws IOException{
        getKeywords(e);

        FXMLLoader root = new FXMLLoader(getClass().getResource("./fxml/MenuBarSizedUp.fxml"));
        stage = (Stage)bioArea.getScene().getWindow();

        LoadHelper.safelyLoad(root, stage);
        
    }

    private void getKeywords(ActionEvent e){
        //burda aynı zamanda embeddingse göndermemiz lazım
        String toEmbed = bioArea.getText();
        interestList.add(toEmbed);
    }

    

    
}

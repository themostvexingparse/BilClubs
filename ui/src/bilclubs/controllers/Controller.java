package bilclubs.controllers;

import java.io.IOException;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.*;
import java.util.ArrayList;

import bilclubs.utils.*;

public class Controller {

    public static Integer userId = null;
    public static String sessionToken = null; 
    public static JSONObject userData = null;
    public static Integer currentClubId = null;

    //instances
    private Stage stage;
    @FXML
    private TextField webmailLoginField;
    @FXML
    private PasswordField webmailPasswordField;
    @FXML 
    private Label wrongInfoLabel;
    @FXML
    private TextField signUpMailField;
    @FXML
    private PasswordField signUpPasswordField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private Label signUpErrorLabel;
    @FXML
    private TextField deptField;
    @FXML
    private Pane designpane;
    @FXML
    private Pane loginPane;
    @FXML
    private Button switchToLogin;
    
    //stack of previous scenes
    public static ArrayList<Scene> backscenes = new ArrayList<>();
    //stack of scenes going forward
    public static ArrayList<Scene> frontscenes = new ArrayList<>();

    @FXML
    public void goToPassword(){
        webmailPasswordField.requestFocus();
    }

    public void goBack(ActionEvent e) throws IOException{
        stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        if(backscenes.size() - 1 > 0){
            stage.setScene(backscenes.get(backscenes.size()-2));
            frontscenes.add(backscenes.get(backscenes.size()-1));
            backscenes.remove(backscenes.size()-1);
        }   
    }

    public void goFront(ActionEvent e) throws IOException{
        stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        if(backscenes.size() - 1 > 0){
            stage.setScene(frontscenes.get(frontscenes.size()-1));
            frontscenes.remove(frontscenes.size()-1);
        }
    }
    
    public void switchToLogin(ActionEvent e) throws IOException{
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/loginscenebuilder.fxml"));

        stage = (Stage)((Node)e.getSource()).getScene().getWindow();

        Scene loginScene = new Scene(loginRoot);
        stage.setScene(loginScene);
        backscenes.add(loginScene);
        stage.show();
    }

    public void switchToSignUp(ActionEvent e) throws IOException{
        Parent signUpRoot = FXMLLoader.load(getClass().getResource("/fxml/signupscenebuilder.fxml"));

        //stage i getiriyoruz
        stage = (Stage)((Node)e.getSource()).getScene().getWindow();

        Scene signUpScene = new Scene(signUpRoot);
        stage.setScene(signUpScene);
        backscenes.add(signUpScene);
        stage.show();
    }


    public void getMailText(ActionEvent e) throws IOException{
        String mail = webmailLoginField.getText();
        String password = webmailPasswordField.getText();
        
        loginRequest(mail, password);
    }


    //helper for getting the sign up information
    public void getSignUpInfo(ActionEvent e) throws IOException{
        String name = nameField.getText();
        String lastName = lastNameField.getText();
        String department = deptField.getText();

        String mail = signUpMailField.getText();
        String password = signUpPasswordField.getText();

        signUpRequest(mail, password, name, lastName, department);

    } 

    public void signUpRequest(String mail, String passWord, String name, String lastname, String dept) throws IOException{
        backscenes.add((Scene)signUpMailField.getScene());

        JSONObject signUpJson = new JSONObject();
        signUpJson.put("firstName", name);
        signUpJson.put("lastName", lastname);
        signUpJson.put("email", mail);
        signUpJson.put("password", passWord);
        signUpJson.put("major", dept);
        signUpJson.put("action", "signup");
        

        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                try{
                    
                    Response signUpResponse = RequestManager.sendPostRequest("api/user", signUpJson);

                    if(signUpResponse.isSuccess()){
                        //EN SON BUNU KOY 
                        // Parent interestRoot = FXMLLoader.load(getClass().getResource("/fxml/interestpage.fxml"));
                        // stage = (Stage)signUpMailField.getScene().getWindow();

                        // Scene interestScene = new Scene(interestRoot);
                        // stage.setScene(interestScene);
                        // stage.show();

                        FXMLLoader interestLoader = new FXMLLoader(getClass().getResource("/fxml/interestpage.fxml"));
                        stage = (Stage)signUpMailField.getScene().getWindow();
                        LoadHelper.safelyLoad(interestLoader, stage);
                    }

                    else{
                        signUpErrorLabel.setText(signUpResponse.getErrorMessage());;
                        signUpErrorLabel.setVisible(true);
                    }
                }
                catch(Exception exception){
                    exception.printStackTrace();
                }
            }

        });
 
    }

    

    private void loginRequest(String username, String passWord) {
        JSONObject responseJson = new JSONObject();
        responseJson.put("email", username);
        responseJson.put("password", passWord);
        responseJson.put("action", "login");

        Pane targetPane = (loginPane != null) ? loginPane : (Pane) webmailLoginField.getScene().lookup("#loginPane");

        System.out.println("lookup result: " + webmailLoginField.getScene().lookup("#loginPane"));

        LoadingSign signFactory = new LoadingSign();
        try { signFactory.showLoadingIcon(targetPane); }
        catch (IOException e) { e.printStackTrace(); }

        new Thread(() -> {
            try {
                Response loginResponse = RequestManager.sendPostRequest("api/user", responseJson);

                Platform.runLater(() -> {
                    try {

                        if (loginResponse.isSuccess()) {
                            sessionToken = loginResponse.getPayload().getString("sessionToken");
                            userId = loginResponse.getPayload().getInt("userId");

                            JSONObject infoRequest = new JSONObject();
                            infoRequest.put("action", "getProfile");
                            infoRequest.put("userId", userId);
                            infoRequest.put("sessionToken", sessionToken);

                            Response userInfoResponse = RequestManager.sendPostRequest("api/user", infoRequest);
                            userData = userInfoResponse.getPayload();

                            stage = (Stage) webmailLoginField.getScene().getWindow();
                            FXMLLoader mainPageFXML = new FXMLLoader(getClass().getResource("/fxml/MenuBarSizedUp.fxml"));
                            LoadHelper.safelyLoad(mainPageFXML, stage);
                        } 
                        
                        else {
                            signFactory.removeLoadingIcon(targetPane);

                            wrongInfoLabel.setText(loginResponse.getErrorMessage());
                            wrongInfoLabel.setVisible(true);
                        }

                    } catch (Exception e) { e.printStackTrace(); }

                });

            } catch (Exception e) { e.printStackTrace(); } 

        }).start();
    }

    public void goHome(ActionEvent e) throws IOException{
        stage = (Stage) switchToLogin.getScene().getWindow();
        FXMLLoader mainPageFXML = new FXMLLoader(getClass().getResource("/fxml/MenuBarSizedUp.fxml"));
        LoadHelper.safelyLoad(mainPageFXML, stage);
    }

    


    

}

package bilclubs.controllers;

import java.io.IOException;

import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SettingsController {
    public static boolean isVisible = true;
    public static boolean isManaged = true;

    @FXML
    Button darkModeToggle;
    @FXML
    Button clubEventToggle;

    @FXML
    Button emailAlertsToggle;

    @FXML
    Button accountButton;
    @FXML
    Button notificationsButton;
    @FXML
    VBox accountBox;
    @FXML
    VBox alertBox;

    @FXML
    TextField firstNameField;
    @FXML
    TextField lastNameField;
    @FXML
    TextField majorField;
    @FXML
    javafx.scene.control.Label saveStatusLabel;

    private org.json.JSONArray cachedInterests = new org.json.JSONArray();
    private String cachedBio = "";

    private boolean clubEventState = true;

    private boolean emailAlertsState = true;

    @FXML
    public void initialize() throws IOException {
        accountBox.setVisible(false);
        accountBox.setManaged(false);
        alertBox.setVisible(false);
        alertBox.setManaged(false);

        loadUserProfile();
    }

    private void loadUserProfile() {
        new Thread(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "getProfile");
                request.put("userId", Controller.userId);
                request.put("sessionToken", Controller.sessionToken);
                request.put("targetUserId", Controller.userId);

                Response response = RequestManager.sendPostRequest("api/user", request);
                if (response.getCode() == 200) {
                    JSONObject data = response.getPayload();
                    Platform.runLater(() -> {
                        firstNameField.setText(data.optString("firstName", ""));
                        lastNameField.setText(data.optString("lastName", ""));
                        majorField.setText(data.optString("major", ""));

                        cachedBio = data.optString("biography", "");

                        cachedInterests = data.optJSONArray("interests");
                        if (cachedInterests == null)
                            cachedInterests = new org.json.JSONArray();

                        clubEventState = data.optBoolean("wantToRecieveClubAndEventAlerts", true);

                        emailAlertsState = data.optBoolean("wantToRecieveMails", true);

                        setTogglePosition(clubEventToggle, clubEventState);

                        setTogglePosition(emailAlertsToggle, emailAlertsState);
                        setTogglePosition(darkModeToggle, Controller.isDarkMode);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setTogglePosition(Button toggle, boolean state) {
        if (toggle == null)
            return;
        toggle.setTranslateX(state ? 24.0 : 0.0);
    }

    private void animateToggle(Button toggle, boolean newState) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), toggle);
        transition.setToX(newState ? 24.0 : 0.0);
        transition.play();
    }

    private void updateProfileField(String fieldName, Object value) {
        new Thread(() -> {
            try {
                JSONObject req = new JSONObject();
                req.put("action", "updateProfile");
                req.put("userId", Controller.userId);
                req.put("sessionToken", Controller.sessionToken);
                req.put(fieldName, value);
                Response response = RequestManager.sendPostRequest("api/user", req);
                if (response.getCode() == 200) {
                    Platform.runLater(() -> {
                        if (saveStatusLabel != null) {
                            saveStatusLabel.setText("Saved!");
                            saveStatusLabel.setVisible(true);
                            saveStatusLabel.setManaged(true);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(2000);
                                } catch (Exception ignore) {
                                }
                                Platform.runLater(() -> {
                                    saveStatusLabel.setText("");
                                    saveStatusLabel.setVisible(false);
                                    saveStatusLabel.setManaged(false);
                                });
                            }).start();
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void saveFirstName(ActionEvent e) {
        updateProfileField("firstName", firstNameField.getText());
    }

    @FXML
    public void saveLastName(ActionEvent e) {
        updateProfileField("lastName", lastNameField.getText());
    }

    @FXML
    public void saveMajor(ActionEvent e) {
        updateProfileField("major", majorField.getText());
    }

    @FXML
    public void toggleClubEvent(ActionEvent e) {
        clubEventState = !clubEventState;
        animateToggle(clubEventToggle, clubEventState);
        updateProfileField("wantToRecieveClubAndEventAlerts", clubEventState);
    }

    @FXML
    public void toggleEmailAlerts(ActionEvent e) {
        emailAlertsState = !emailAlertsState;
        animateToggle(emailAlertsToggle, emailAlertsState);
        updateProfileField("wantToRecieveMails", emailAlertsState);
    }

    @FXML
    public void account(ActionEvent e) throws IOException {
        boolean currentState = accountBox.isVisible();
        accountBox.setVisible(!currentState);
        accountBox.setManaged(!currentState);
        if (!currentState) {
            alertBox.setVisible(false);
            alertBox.setManaged(false);
        }
    }

    @FXML
    public void notifications(ActionEvent e) throws IOException {
        boolean currentState = alertBox.isVisible();
        alertBox.setVisible(!currentState);
        alertBox.setManaged(!currentState);
        if (!currentState) {
            accountBox.setVisible(false);
            accountBox.setManaged(false);
        }
    }

    @FXML
    public void toggle(ActionEvent e) {
        Controller.isDarkMode = !Controller.isDarkMode;
        animateToggle(darkModeToggle, Controller.isDarkMode);

        if (darkModeToggle.getScene() != null) {
            ObservableList<String> classes = darkModeToggle.getScene().getRoot().getStyleClass();
            if (classes.contains("darkmode") && !Controller.isDarkMode) {
                classes.remove("darkmode");
            } else if (!classes.contains("darkmode") && Controller.isDarkMode) {
                classes.add("darkmode");
            }
        }
    }

    @FXML
    public void changeInterests(ActionEvent e) {
        try {
            FXMLLoader interestLoader = new FXMLLoader(getClass().getResource("/fxml/interestpage.fxml"));
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(interestLoader.load()));

            InterestController intCtrl = interestLoader.getController();
            intCtrl.setPopupMode(popupStage);
            intCtrl.setPreloadedInterests(cachedInterests);

            popupStage.showAndWait();
            loadUserProfile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void changeBiography(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/interestKeywords.fxml"));
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(loader.load()));

            InterestController intCtrl = loader.getController();
            intCtrl.setPopupMode(popupStage);
            intCtrl.setPreloadedBio(cachedBio);

            popupStage.showAndWait();
            loadUserProfile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

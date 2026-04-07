package bilclubs.controllers;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;
import bilclubs.components.SearchResultPane;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

public class SearchController {
    @FXML
    private VBox resultsContainer;

    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> searchPicker;

    @FXML
    public void initialize() {
        searchPicker.getItems().add("Club");
        searchPicker.getItems().add("Event");
        searchPicker.getSelectionModel().selectFirst();

    }

    public void search(Event e) {
        String searchVal = searchField.getText();
        System.out.println(searchVal);

        String searchType = searchPicker.getSelectionModel().getSelectedItem();
        System.out.println(searchType);

        try {
            JSONObject request = new JSONObject();
            request.put("userId", Controller.userId);
            request.put("sessionToken", Controller.sessionToken);
            request.put("action", "search");
            request.put("query", searchVal);

            String endpoint = searchType.equals("Club") ? "api/club" : "api/event";
            Response response = RequestManager.sendPostRequest(endpoint, request);

            if (response.isSuccess()) {
                Integer count = response.getPayload().optIntegerObject("count", 0);
                JSONArray results = response.getPayload().optJSONArray("results");
                if (results == null) {
                    results = new JSONArray();
                }

                System.out.println("Found " + count + " results");

                final JSONArray finalResults = results;
                Platform.runLater(() -> {
                    resultsContainer.getChildren().clear();
                    for (int i = 0; i < finalResults.length(); i++) {
                        JSONObject res = finalResults.getJSONObject(i);
                        SearchResultPane pane = new SearchResultPane(res, searchType);
                        resultsContainer.getChildren().add(pane);
                    }
                });
            } else {
                System.out.println("Search failed: " + response.getErrorMessage());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        searchField.selectAll();
    }

    @FXML
    public void searchClicked(MouseEvent event) {
        search(null);
    }

}

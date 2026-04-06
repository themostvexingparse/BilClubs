package bilclubs.controllers;

import javafx.scene.input.KeyEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class SearchController {
    @FXML private TextField searchField;
    @FXML private ChoiceBox searchPicker;

    @FXML
    public void initialize(){
        searchPicker.getItems().add("Club");
        searchPicker.getItems().add("Event");
        searchPicker.getSelectionModel().selectFirst();

    }

    public void search(Event e){
        String searchVal = searchField.getText();
        System.out.println(searchVal);

        String searchType = (String) searchPicker.getSelectionModel().getSelectedItem();
        System.out.println(searchType);

        searchField.selectAll();
    }


}

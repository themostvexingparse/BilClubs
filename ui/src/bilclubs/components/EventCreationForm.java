package bilclubs.components;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EventCreationForm extends Stage{

    public EventCreationForm() throws IOException{
        Parent form = FXMLLoader.load(getClass().getResource("/fxml/EventForm.fxml"));
        Scene formScene = new Scene(form);
        this.setScene(formScene);
        this.show();
    }
}
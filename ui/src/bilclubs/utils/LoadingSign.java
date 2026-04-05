package bilclubs.utils;

import java.io.IOException;
import java.util.HashMap;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class LoadingSign {
    Integer spinnerId = null;
    HashMap<Integer, Boolean> visibiliyCodes = new HashMap<>();
    
    public void showLoadingIcon(Pane pane) throws IOException{
        Parent spinner = FXMLLoader.load(getClass().getResource("/fxml/load.fxml"));
        ObservableList<Node> childrenNode = pane.getChildren();
        for(Node node : childrenNode){
            visibiliyCodes.put(node.hashCode(), ((Boolean)(true == node.isVisible())));
            node.setVisible(false);
        }

        pane.getChildren().add(spinner);
        spinnerId = spinner.hashCode();

        //TODO: spinner gelcek dönsün falan
        //IDyi setle

    }

    public void removeLoadingIcon(Pane pane) throws IOException{

        pane.getChildren().removeIf(node -> node.hashCode() == spinnerId);

        ObservableList<Node> childrenNode = pane.getChildren();
        for(Node node : childrenNode){
            // if (node.hashCode() == spinnerId) {
            //     pane.getChildren().remove(node);
            //     continue;
            // }
            Boolean visibility = visibiliyCodes.getOrDefault(node.hashCode(), null);
            if (visibility == null) continue;
            node.setVisible(visibility);
        }

    }
}

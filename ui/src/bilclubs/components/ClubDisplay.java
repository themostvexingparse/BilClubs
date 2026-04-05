package bilclubs.components;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class ClubDisplay extends Pane {
    
    @FXML private ImageView clubImage;
    @FXML private Label namelbl;
    @FXML private Label desclbl;

    //instances
    private String name;
    private String desc;

    public ClubDisplay() throws IOException{
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/userClubPane.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public void setName(String aName){
        this.name = aName;
        namelbl.setText(name);
        
    }

    public void setDesc(String aDesc){
        this.desc = aDesc;
        desclbl.setText(desc);
    }

}

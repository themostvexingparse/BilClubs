package bilclubs.utils;

import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

public class ThemeManager {

    public static boolean darkmode = false;

    public static void setTheme(Pane root){
        ObservableList<String> style = root.getStyleClass();
        if(style.contains("darkmode")){
            style.remove("darkmode");
        }

        else{
            style.add("darkmode");
        }

    }

    public static void switchMode(Pane root){
        darkmode = !darkmode;
        setTheme(root);
    }
}

package bilclubs.components;

import java.io.IOException;

import org.json.JSONObject;

import bilclubs.controllers.Controller;
import bilclubs.utils.LoadHelper;
import bilclubs.utils.RequestManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBox;

public class SearchResultPane extends HBox {

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Rectangle imageRect;

    private JSONObject data;
    private String type;

    @FXML
    public void initialise() throws IOException {
        FXMLLoader backbone = new FXMLLoader(getClass().getResource("/fxml/searchResultPane.fxml"));
        backbone.setRoot(this);
        backbone.setController(this);
        backbone.load();
    }

    public SearchResultPane(JSONObject data, String type) {
        this.data = data;
        this.type = type;
        try {
            initialise();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String title = data.optString("name", "Unknown");
                String description = data.optString("description", "");
                titleLabel.setText(title);
                descriptionLabel.setText(description);

                if (type.equals("Club")) {
                    subtitleLabel.setVisible(false);
                    subtitleLabel.setManaged(false);
                } else {
                    subtitleLabel.setText(data.optString("clubName", "Event"));
                    subtitleLabel.setVisible(true);
                    subtitleLabel.setManaged(true);
                }

                String imageUrl = type.equals("Club") ? data.optString("iconFilename", "")
                        : data.optString("posterImage", "");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = data.optString("iconFilename", ""); // fallback
                }
                String defaultPath = type.equals("Club") ? "/assets/default-club-icon.png"
                        : "/assets/default-event-poster.jpg";
                Image defaultImg = new Image(getClass().getResourceAsStream(defaultPath));
                imageRect.setFill(new ImagePattern(type.equals("Event") ? cropToSquare(defaultImg) : defaultImg));

                if (!imageUrl.isEmpty() && !imageUrl.contains("default")) {
                    Image img = new Image(RequestManager.defaultAddress + imageUrl, true);
                    if (img.getProgress() >= 1.0) {
                        if (!img.isError())
                            imageRect.setFill(new ImagePattern(type.equals("Event") ? cropToSquare(img) : img));
                    } else {
                        img.progressProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal.doubleValue() >= 1.0 && !img.isError()) {
                                imageRect.setFill(new ImagePattern(type.equals("Event") ? cropToSquare(img) : img));
                            }
                        });
                    }
                }
            }
        });
    }

    @FXML
    public void handleCardClick(MouseEvent event) {
        try {
            AnchorPane contentPane = (AnchorPane) this.getScene().lookup("#rightAnchor");
            if (contentPane == null)
                return;

            if (type.equals("Club")) {
                Controller.currentClubId = data.optInt("id", -1);
                FXMLLoader clubPage = new FXMLLoader(getClass().getResource("/fxml/ClubPage.fxml"));
                LoadHelper.safelyLoad(clubPage, contentPane);
            } else {
                data.put("eventId", data.optInt("id", data.optInt("eventId", -1)));
                Controller.currentEventObject = data;
                FXMLLoader eventPage = new FXMLLoader(getClass().getResource("/fxml/eventPage.fxml"));
                LoadHelper.safelyLoad(eventPage, contentPane);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Image cropToSquare(Image img) {
        double width = img.getWidth();
        double height = img.getHeight();
        if (width == height || width <= 0 || height <= 0)
            return img;

        double size = Math.min(width, height);
        double x = (width - size) / 2.0;
        double y = (height - size) / 2.0;

        try {
            return new javafx.scene.image.WritableImage(img.getPixelReader(), (int) x, (int) y, (int) size, (int) size);
        } catch (Exception e) {
            return img;
        }
    }
}

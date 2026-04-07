package bilclubs.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import bilclubs.utils.RequestManager;
import bilclubs.utils.Response;

public class CalendarController implements Initializable {

    ZonedDateTime dateFocus;
    ZonedDateTime today;

    @FXML
    private Text year;

    @FXML
    private Text month;

    @FXML
    private FlowPane calendar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateFocus = ZonedDateTime.now();
        today = ZonedDateTime.now();
        Platform.runLater(() -> drawCalendar());
    }

    @FXML
    void backOneMonth(ActionEvent event) {
        dateFocus = dateFocus.minusMonths(1);
        calendar.getChildren().clear();
        Platform.runLater(() -> drawCalendar());
    }

    @FXML
    void forwardOneMonth(ActionEvent event) {
        dateFocus = dateFocus.plusMonths(1);
        calendar.getChildren().clear();
        Platform.runLater(() -> drawCalendar());
    }

    private void drawCalendar() {
        year.setText(String.valueOf(dateFocus.getYear()));
        month.setText(String.valueOf(dateFocus.getMonth()));

        double calendarWidth = calendar.getPrefWidth();
        double calendarHeight = calendar.getPrefHeight();
        double strokeWidth = 1;
        double spacingH = calendar.getHgap();
        double spacingV = calendar.getVgap();

        JSONObject eventReq = new JSONObject();
        eventReq.put("action", "getUpcomingEvents");
        eventReq.put("userId", Controller.userId);
        eventReq.put("userSpecific", true);
        eventReq.put("sessionToken", Controller.sessionToken);

        Response eventResponse = RequestManager.sendPostRequest("api/user", eventReq);
        JSONArray userEvents = eventResponse.getPayload().optJSONArray("events");
        if (userEvents == null) userEvents = new JSONArray();
        Map<Integer, List<JSONObject>> events = getCalendarActivitiesMonth(userEvents);

        int monthMaxDate = dateFocus.getMonth().length(java.time.Year.isLeap(dateFocus.getYear()));

        int dateOffset = ZonedDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), 1, 0, 0, 0, 0, dateFocus.getZone())
                .getDayOfWeek().getValue() - 1;

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                StackPane stackPane = new StackPane();
                Rectangle rectangle = new Rectangle();

                if (!Controller.isDarkMode) {
                    rectangle.setFill(Color.TRANSPARENT);
                } else {
                    rectangle.setFill(Color.rgb(86, 101, 122));
                }

                rectangle.setStroke(month.getFill());
                rectangle.setStrokeWidth(strokeWidth);
                double rectangleWidth = (calendarWidth / 7) - strokeWidth - spacingH;
                rectangleWidth = Math.max(rectangleWidth, 50);
                rectangle.setWidth(rectangleWidth);
                double rectangleHeight = (calendarHeight / 6) - strokeWidth - spacingV;
                rectangleHeight = Math.max(rectangleHeight, 50);
                rectangle.setHeight(rectangleHeight);

                DropShadow rectShadow = new DropShadow();
                rectShadow.setColor(Color.color(0, 0, 0, 0.3));
                rectangle.setEffect(rectShadow);
                rectangle.setArcWidth(20);
                rectangle.setArcHeight(20);
                stackPane.getChildren().add(rectangle);

                stackPane.setPrefSize(rectangleWidth, rectangleHeight);
                stackPane.setMaxSize(rectangleWidth, rectangleHeight);
                stackPane.setMinSize(rectangleWidth, rectangleHeight);

                int calculatedDate = (j + 1) + (7 * i);
                if (calculatedDate > dateOffset) {
                    int currentDate = calculatedDate - dateOffset;
                    if (currentDate <= monthMaxDate) {

                        Text date = new Text(String.valueOf(currentDate));
                        StackPane.setAlignment(date, javafx.geometry.Pos.TOP_LEFT);
                        date.setTranslateX(6);
                        date.setTranslateY(18);
                        stackPane.getChildren().add(date);

                        List<JSONObject> calendarActivities = events.get(currentDate);
                        if (calendarActivities != null) {
                            createCalendarActivity(calendarActivities, rectangleHeight, rectangleWidth, stackPane);
                        }
                    }
                    if (today.getYear() == dateFocus.getYear() && today.getMonth() == dateFocus.getMonth() && today.getDayOfMonth() == currentDate) {
                        rectangle.setStroke(Color.BLUE);
                    }
                }
                calendar.getChildren().add(stackPane);
            }
        }
    }

    private void createCalendarActivity(List<JSONObject> calendarActivities, double rectangleHeight, double rectangleWidth, StackPane stackPane) {
        VBox calendarActivityBox = new VBox();
        for (int k = 0; k < calendarActivities.size(); k++) {
            if (k >= 2) {
                Text moreActivities = new Text("...");
                calendarActivityBox.getChildren().add(moreActivities);
                moreActivities.setOnMouseClicked(mouseEvent -> System.out.println(calendarActivities));
                break;
            }
            String name = calendarActivities.get(k).getString("name");
            String rawDate = calendarActivities.get(k).getString("startDate");
            String time = LocalDateTime.parse(rawDate.replace(" ", "T")).toLocalTime().toString().substring(0, 5);
            String displayName = name.length() > 15 ? name.substring(0, 15) + "…" : name;
            Text text = new Text(displayName + " " + time);
            text.setStyle("-fx-font-size: 10px;");
            calendarActivityBox.getChildren().add(text);
            text.setOnMouseClicked(mouseEvent -> System.out.println(text.getText()));
        }

        StackPane.setAlignment(calendarActivityBox, javafx.geometry.Pos.TOP_LEFT);
        calendarActivityBox.setTranslateX(4);
        calendarActivityBox.setTranslateY(28);

        calendarActivityBox.setMaxWidth(rectangleWidth * 0.8);
        calendarActivityBox.setMaxHeight(rectangleHeight * 0.65);

        calendarActivityBox.setStyle("-fx-background-color:-menu-blue; -fx-background-radius:10;");

        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.color(0, 0, 0, 0.3));
        calendarActivityBox.setEffect(textShadow);

        Rectangle clip = new Rectangle(rectangleWidth * 0.8, rectangleHeight * 0.65);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        calendarActivityBox.setClip(clip);

        stackPane.getChildren().add(calendarActivityBox);
    }

    private Map<Integer, List<JSONObject>> createCalendarMap(List<JSONObject> calendarActivities) {
        Map<Integer, List<JSONObject>> calendarActivityMap = new HashMap<>();

        for (JSONObject activity : calendarActivities) {
            try {
                String activityDate = activity.getString("startDate");
                System.out.println("RAW DATE: " + activityDate);

                LocalDate date = LocalDateTime.parse(activityDate.replace(" ", "T")).toLocalDate();
                int dayOfMonth = date.getDayOfMonth();

                calendarActivityMap.computeIfAbsent(dayOfMonth, k -> new ArrayList<>()).add(activity);

            } catch (Exception e) {
                System.out.println("HATA VAR → " + activity);
                e.printStackTrace();
            }
        }
        return calendarActivityMap;
    }

    private Map<Integer, List<JSONObject>> getCalendarActivitiesMonth(JSONArray events) {
        List<JSONObject> calendarActivities = new ArrayList<>();

        for (int i = 0; i < events.length(); i++) {

            JSONObject event = events.getJSONObject(i);
            try {
                String dateStr = event.getString("startDate");
                calendarActivities.add(event);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return createCalendarMap(calendarActivities);
    }
}
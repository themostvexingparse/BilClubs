package bilclubs.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    private Label year;

    @FXML
    private Label month;

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
        double spacingH = calendar.getHgap();
        double spacingV = calendar.getVgap();

        JSONObject eventReq = new JSONObject();
        eventReq.put("action", "getUpcomingEvents");
        eventReq.put("userId", Controller.userId);
        eventReq.put("userSpecific", true);
        eventReq.put("sessionToken", Controller.sessionToken);

        Response eventResponse = RequestManager.sendPostRequest("api/user", eventReq);
        JSONArray userEvents = eventResponse.getPayload().optJSONArray("events");
        if (userEvents == null)
            userEvents = new JSONArray();
        Map<Integer, List<JSONObject>> events = getCalendarActivitiesMonth(userEvents);

        int monthMaxDate = dateFocus.getMonth().length(java.time.Year.isLeap(dateFocus.getYear()));
        int dateOffset = ZonedDateTime
                .of(dateFocus.getYear(), dateFocus.getMonthValue(), 1, 0, 0, 0, 0, dateFocus.getZone())
                .getDayOfWeek().getValue() - 1;

        double cellWidth = (calendarWidth / 7) - spacingH;
        double cellHeight = (calendarHeight / 6) - spacingV;
        cellWidth = Math.max(cellWidth, 50);
        cellHeight = Math.max(cellHeight, 50);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                StackPane stackPane = new StackPane();
                stackPane.setPrefSize(cellWidth, cellHeight);
                stackPane.getStyleClass().add("cal-cell");

                int calculatedDate = (j + 1) + (7 * i);
                if (calculatedDate > dateOffset) {
                    int currentDate = calculatedDate - dateOffset;
                    if (currentDate <= monthMaxDate) {

                        Label dateLabel = new Label(String.valueOf(currentDate));
                        dateLabel.getStyleClass().add("cal-date-num");
                        StackPane.setAlignment(dateLabel, javafx.geometry.Pos.TOP_LEFT);
                        dateLabel.setTranslateX(6);
                        dateLabel.setTranslateY(6);
                        stackPane.getChildren().add(dateLabel);

                        List<JSONObject> calendarActivities = events.get(currentDate);
                        if (calendarActivities != null && !calendarActivities.isEmpty()) {
                            createCalendarActivity(calendarActivities, cellHeight, cellWidth, stackPane);
                        }

                        if (today.getYear() == dateFocus.getYear() && today.getMonth() == dateFocus.getMonth()
                                && today.getDayOfMonth() == currentDate) {
                            stackPane.getStyleClass().add("cal-cell-today");
                        }
                    } else {
                        stackPane.getStyleClass().add("cal-cell-inactive");
                    }
                } else {
                    stackPane.getStyleClass().add("cal-cell-inactive");
                }

                calendar.getChildren().add(stackPane);
            }
        }
    }

    private void createCalendarActivity(List<JSONObject> calendarActivities, double rectangleHeight,
            double rectangleWidth, StackPane stackPane) {
        VBox calendarActivityBox = new VBox();
        calendarActivityBox.setSpacing(2);

        for (int k = 0; k < calendarActivities.size(); k++) {
            if (k >= 2) {
                int remaining = calendarActivities.size() - 2;
                Label moreActivities = new Label("+" + remaining + " more");
                moreActivities.getStyleClass().add("cal-overflow");
                calendarActivityBox.getChildren().add(moreActivities);
                break;
            }
            JSONObject eventData = calendarActivities.get(k);
            String name = eventData.getString("name");
            String rawDate = eventData.getString("startDate");
            String time = LocalDateTime.parse(rawDate.replace(" ", "T")).toLocalTime().toString().substring(0, 5);
            String displayName = name.length() > 10 ? name.substring(0, 10) + "…" : name;

            Label textInfo = new Label(displayName + " " + time);
            textInfo.getStyleClass().add("cal-event-chip");
            textInfo.setPrefWidth(rectangleWidth - 16); // padding

            Tooltip tooltip = new Tooltip(name);
            textInfo.setTooltip(tooltip);

            textInfo.setOnMouseClicked(mouseEvent -> {
                try {
                    // Similar to SearchResultPane logic
                    AnchorPane contentPane = (AnchorPane) textInfo.getScene().lookup("#rightAnchor");
                    if (contentPane != null) {
                        eventData.put("eventId", eventData.optInt("id", eventData.optInt("eventId", -1)));
                        Controller.currentEventObject = eventData;
                        FXMLLoader eventPage = new FXMLLoader(getClass().getResource("/fxml/eventPage.fxml"));
                        bilclubs.utils.LoadHelper.safelyLoad(eventPage, contentPane);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            calendarActivityBox.getChildren().add(textInfo);
        }

        StackPane.setAlignment(calendarActivityBox, javafx.geometry.Pos.TOP_LEFT);
        calendarActivityBox.setTranslateX(8);
        calendarActivityBox.setTranslateY(25); // Push down below date number

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
                LocalDate date = LocalDateTime.parse(dateStr.replace(" ", "T")).toLocalDate();
                if (date.getYear() == dateFocus.getYear() && date.getMonth() == dateFocus.getMonth()) {
                    calendarActivities.add(event);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return createCalendarMap(calendarActivities);
    }
}
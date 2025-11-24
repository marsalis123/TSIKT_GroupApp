package com.example.test_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class CalendarPane extends VBox {
    private final User user;
    private final UserManager userManager;
    private final List<Group> userGroups;
    private YearMonth currentMonth = YearMonth.now();
    private int selectedGroup = -1;

    private GridPane calendarGrid;
    private Label monthLabel;
    private VBox dayEventDetailBox;
    private NotificationBell notificationBell;

    private static final String[] COLORS = {"#2196f3","#43a047","#FFC107","#e53935","#8e24aa"};

    public CalendarPane(User user, UserManager userManager, List<Group> userGroups) {
        this.user = user;
        this.userManager = userManager;
        this.userGroups = userGroups;
        setSpacing(14);
        setPadding(new Insets(16,24,24,24));
        setStyle("-fx-background-color:transparent;");
        buildLayout();
    }

    private void buildLayout() {
        getChildren().clear();

        HBox topBar = new HBox(18);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button prevMonthBtn = new Button("<");
        Button nextMonthBtn = new Button(">");

        monthLabel = new Label(currentMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("sk")) + " " + currentMonth.getYear());
        monthLabel.setFont(Font.font("Arial", 27));
        monthLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#d84315;");
        monthLabel.setMinWidth(140);

        prevMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            buildLayout();
        });
        nextMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            buildLayout();
        });

        ComboBox<Group> groupCombo = new ComboBox<>();
        groupCombo.getItems().add(new Group(-1, "Všetky skupiny"));
        groupCombo.getItems().addAll(userGroups);
        groupCombo.getSelectionModel().selectFirst();
        groupCombo.setMinWidth(140);
        groupCombo.setConverter(new StringConverter<Group>() {
            public String toString(Group g) { return g.name; }
            public Group fromString(String s) { return null; }
        });
        groupCombo.setOnAction(e -> {
            selectedGroup = groupCombo.getValue().id;
            buildLayout();
        });

        Button addBtn = new Button("+ Pridať termín");
        addBtn.setStyle("-fx-background-radius:22; -fx-background-color:#6dc6e9; -fx-text-fill:#fff; -fx-font-size:16px; -fx-font-weight:bold;");
        addBtn.setOnAction(e -> showEventDialog(null, LocalDate.now()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Notifikačný zvonček vlož vždy na koniec topBaru
        notificationBell = new NotificationBell(this::getDeadlineNotifications);
        notificationBell.updateNotifications(getDeadlineNotifications());
        topBar.getChildren().addAll(prevMonthBtn, monthLabel, nextMonthBtn, new Label("| Skupina:"), groupCombo, addBtn, spacer, notificationBell);

        getChildren().add(topBar);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(6);
        calendarGrid.setVgap(7);
        calendarGrid.setPadding(new Insets(12,0,2,0));
        renderCalendarGrid();

        getChildren().add(calendarGrid);

        dayEventDetailBox = new VBox();
        getChildren().add(dayEventDetailBox);
    }

    // Vráti upozornenia pre zvonček (tu len deadliny z kalendára, môžeš rozšíriť)
    private List<String> getDeadlineNotifications() {
        LocalDate today = LocalDate.now();
        List<String> notifyEvents = new ArrayList<>();
        for (Group g : userGroups) {
            for (CalendarEvent e : userManager.getCalendarEvents(g.id, today.toString().substring(0, 7))) {
                LocalDate date = LocalDate.parse(e.date);
                if (e.notify && (date.equals(today) || (!date.isBefore(today) && date.minusDays(3).isBefore(today))))
                    notifyEvents.add(e.title + " (" + e.date + ")");
            }
        }
        return notifyEvents;
    }

    private void renderCalendarGrid() {
        calendarGrid.getChildren().clear();

        String[] daysSk = {"Po","Ut","St","Št","Pi","So","Ne"};
        for (int i=0;i<7;i++) {
            Label day = new Label(daysSk[i]);
            day.setFont(Font.font("Arial",15)); day.setStyle("-fx-opacity:0.86;-fx-font-weight:bold;");
            day.setAlignment(Pos.CENTER); day.setMinWidth(50);
            calendarGrid.add(day, i, 0);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int firstDayIdx = (firstOfMonth.getDayOfWeek().getValue() + 6)%7;
        int daysInMonth = currentMonth.lengthOfMonth();

        Map<Integer, List<CalendarEvent>> eventsByDay = new HashMap<>();
        List<CalendarEvent> events;
        if (selectedGroup <= 0) {
            events = new ArrayList<>();
            for (Group g : userGroups) {
                events.addAll(userManager.getCalendarEvents(g.id, currentMonth.toString()));
            }
        } else {
            events = userManager.getCalendarEvents(selectedGroup, currentMonth.toString());
        }

        for (CalendarEvent evt : events) {
            int day = LocalDate.parse(evt.date).getDayOfMonth();
            if (!eventsByDay.containsKey(day)) eventsByDay.put(day, new ArrayList<>());
            eventsByDay.get(day).add(evt);
        }

        int row=1, col=firstDayIdx;
        for (int d=1; d<=daysInMonth; d++) {
            VBox dayCell = new VBox(3);
            dayCell.setPadding(new Insets(7,3,3,6));
            dayCell.setStyle("-fx-background-radius:18; -fx-background-color:#fff8f4;");
            dayCell.setAlignment(Pos.TOP_LEFT);
            dayCell.setMinSize(56,62);
            dayCell.setMaxSize(62,76);

            Label dayNum = new Label(String.valueOf(d));
            dayNum.setFont(Font.font("Arial",17));
            dayNum.setStyle("-fx-font-weight:bold; -fx-text-fill:#9e4800;");

            dayCell.getChildren().add(dayNum);

            if (eventsByDay.containsKey(d)) {
                for (CalendarEvent ev : eventsByDay.get(d)) {
                    Label evBubble = new Label(ev.title);
                    evBubble.setFont(Font.font("Arial", 12));
                    evBubble.setPadding(new Insets(2,8,2,8));
                    evBubble.setTextFill(Color.web("#fff"));
                    evBubble.setTextAlignment(TextAlignment.CENTER);
                    String neon = ev.notify ? "-fx-border-color:#ff00e0; -fx-border-width:2px;" : "";
                    evBubble.setStyle("-fx-background-radius:11; -fx-background-color:" + ev.color + "; -fx-font-weight:bold;" + neon);
                    evBubble.setOnMouseClicked(e -> showEventDetail(ev));
                    dayCell.getChildren().add(evBubble);
                }
            }
            final int dayConst = d;
            dayCell.setOnMouseClicked(e -> showDayEvents(dayConst));
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col>6) { col=0; row++; }
        }
    }

    private void showDayEvents(int day) {
        dayEventDetailBox.getChildren().clear();
        LocalDate date = currentMonth.atDay(day);
        List<CalendarEvent> events;
        if (selectedGroup <= 0) {
            events = new ArrayList<>();
            for (Group g : userGroups) {
                events.addAll(userManager.getCalendarEvents(g.id, currentMonth.toString()));
            }
        } else {
            events = userManager.getCalendarEvents(selectedGroup, currentMonth.toString());
        }

        List<CalendarEvent> filtered = new ArrayList<>();
        for (CalendarEvent evt : events) {
            if (evt.date.equals(date.toString())) filtered.add(evt);
        }
        Label hdr = new Label("Termíny pre: " + date.toString());
        hdr.setFont(Font.font("Arial", 17));
        hdr.setStyle("-fx-font-weight:bold; -fx-text-fill:#b76013;");
        VBox eventList = new VBox(8);
        for (CalendarEvent ev : filtered) {
            HBox bbl = new HBox(12);
            Rectangle colored = new Rectangle(13,13,Color.web(ev.color));
            Label t = new Label(ev.title + "  ");
            t.setFont(Font.font("Arial", 14));
            Label desc = new Label(ev.description==null?"":ev.description);
            desc.setFont(Font.font("Arial",12));
            bbl.getChildren().addAll(colored, t, desc);
            bbl.setOnMouseClicked(e -> showEventDetail(ev));
            eventList.getChildren().add(bbl);
        }
        Button addBtn = new Button("+ Pridať termín na tento deň");
        addBtn.setOnAction(e -> showEventDialog(null, date));
        dayEventDetailBox.getChildren().addAll(hdr,eventList,addBtn);
    }

    private void showEventDialog(CalendarEvent editEv, LocalDate date) {
        Dialog<Void> diag = new Dialog<>();
        diag.setTitle(editEv==null?"Pridať termín":"Upraviť termín");

        VBox layout = new VBox(11);
        layout.setPadding(new Insets(17));
        layout.setMinWidth(340);

        ComboBox<Group> groupCb = new ComboBox<>();
        groupCb.getItems().addAll(userGroups);
        if (editEv!=null)
            for (Group g:userGroups) if (g.id==editEv.groupId) groupCb.getSelectionModel().select(g);
            else if (!userGroups.isEmpty()) groupCb.getSelectionModel().selectFirst();

        TextField titleFld = new TextField(editEv==null?"":editEv.title);
        titleFld.setPromptText("Názov termínu");

        TextArea descFld = new TextArea(editEv==null?"":editEv.description);
        descFld.setPromptText("Popis");

        DatePicker datePick = new DatePicker(editEv==null?date:LocalDate.parse(editEv.date));
        ColorPicker colorPick = new ColorPicker(editEv==null
                ?Color.web(COLORS[new Random().nextInt(COLORS.length)]) : Color.web(editEv.color));

        CheckBox notif = new CheckBox("Upozornenie");
        notif.setSelected(editEv!=null && editEv.notify);

        layout.getChildren().addAll(
                new Label("Skupina:"), groupCb,
                new Label("Dátum:"), datePick,
                new Label("Názov:"), titleFld,
                new Label("Popis:"), descFld,
                new Label("Farba bubliny:"), colorPick,
                notif
        );
        ButtonType saveType = new ButtonType("Uložiť", ButtonBar.ButtonData.APPLY);
        diag.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CLOSE);
        diag.getDialogPane().setContent(layout);

        diag.setResultConverter(btn -> {
            if (btn == saveType) {
                Group sel = groupCb.getValue();
                if (sel==null || titleFld.getText().trim().isEmpty()) return null;
                if (editEv==null) {
                    userManager.addCalendarEvent(new CalendarEvent(
                            -1, sel.id, user.getId(),
                            titleFld.getText().trim(),
                            descFld.getText(),
                            datePick.getValue().toString(),
                            toWebColor(colorPick.getValue()),
                            notif.isSelected(), ""
                    ));
                }
                buildLayout();
                notificationBell.updateNotifications(getDeadlineNotifications());
            }
            return null;
        });
        diag.showAndWait();
    }

    private String toWebColor(javafx.scene.paint.Color c) {
        return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    private void showEventDetail(CalendarEvent event) {
        dayEventDetailBox.getChildren().clear();
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        Label hdr = new Label(event.title);
        hdr.setFont(Font.font("Arial",19));
        hdr.setStyle("-fx-font-weight:bold; -fx-text-fill:"+event.color+";");
        box.getChildren().add(hdr);

        box.getChildren().add(new Label("Dátum: " + event.date));
        for (Group g: userGroups) if (g.id==event.groupId)
            box.getChildren().add(new Label("Skupina: "+ g.name));
        if (event.description!=null && !event.description.isEmpty())
            box.getChildren().add(new Label("Popis: "+event.description));
        if (event.notify)
            box.getChildren().add(new Label("Upozornenie: Áno"));

        Button closeBtn = new Button("Zavrieť");
        closeBtn.setOnAction(e -> dayEventDetailBox.getChildren().clear());
        box.getChildren().add(closeBtn);

        dayEventDetailBox.getChildren().add(box);
    }
}

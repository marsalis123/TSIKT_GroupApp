package com.example.test_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.List;

public class MainMenu extends BorderPane {
    private final MainApp app;
    private final User user;
    private final UserManager userManager;
    private List<Group> userGroups;

    // Zvonček pre notifikácie
    private NotificationBell notificationBell;

    public MainMenu(MainApp app, User user, UserManager userManager) {
        this.app = app;
        this.user = user;
        this.userManager = userManager;
        this.userGroups = userManager.getUserGroups(user.getUsername()); // pre Kalendár a Práce

        setStyle("-fx-background-color:linear-gradient(120deg, #cbaf97 0%, #b4845a 54%, #795d42 100%);");
        setPrefSize(1200, 900);

        createLayout();
    }

    private void createLayout() {
        // Header v hornej casti - meno/pfp + zvonček
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView pfp = new ImageView();
        if (user.getPhotoPath() != null && !user.getPhotoPath().isEmpty()) {
            try {
                Image img = new Image("file:" + user.getPhotoPath(), 54, 54, true, true);
                pfp.setImage(img);
            } catch (Exception e) { }
        }
        pfp.setFitWidth(54); pfp.setFitHeight(54);
        pfp.setClip(new Circle(27, 27, 27));
        pfp.setStyle("-fx-effect: dropshadow(gaussian, #ffa726, 8,0.13,0,2);");

        VBox nameBox = new VBox(
                new Label("Prihlásený ako"),
                new Label(user.getName())
        );
        ((Label) nameBox.getChildren().get(0)).setFont(Font.font("Arial", 15));
        ((Label) nameBox.getChildren().get(0)).setStyle("-fx-text-fill:#aa6f1a;");
        ((Label) nameBox.getChildren().get(1)).setFont(Font.font("Arial", 22));
        ((Label) nameBox.getChildren().get(1)).setTextFill(Color.web("#d84315"));
        ((Label) nameBox.getChildren().get(1)).setStyle("-fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Zvonček – existujúci NotificationBell, ktorý už používaš
        // predpoklad: má konštruktor NotificationBell(Supplier<List<String>>)
        notificationBell = new NotificationBell(this::getDeadlineNotifications);

        header.getChildren().addAll(pfp, nameBox, spacer, notificationBell);
        header.setPadding(new Insets(23, 24, 16, 38));
        setTop(header);

        // Sidebar menu
        VBox sidebar = new VBox(16);
        sidebar.setPadding(new Insets(32, 7, 0, 22));
        sidebar.setMinWidth(210);
        sidebar.setStyle("-fx-background-radius:30; -fx-background-color:rgba(180, 132, 90, 0.16);");
        sidebar.setAlignment(Pos.TOP_LEFT);

        Button skupinyBtn = basicNavBtn("Moje skupiny");
        skupinyBtn.setOnAction(e -> setCenter(new MojeSkupinyPane(user, app, userManager)));

        Button praceBtn = basicNavBtn("Moje práce");
        praceBtn.setOnAction(e -> setCenter(new MojePracePane(user, userManager)));

        Button kalendarBtn = basicNavBtn("Kalendár / Termíny");
        kalendarBtn.setOnAction(e -> {
            // pri otvorení kalendára si obnov skupiny zo servera
            userGroups = userManager.getUserGroups(user.getUsername());
            setCenter(new CalendarPane(user, userManager, userGroups));
        });

        Button profilBtn = basicNavBtn("Môj profil");
        profilBtn.setOnAction(e -> app.showProfileScreen(user));

        Button odhlasBtn = new Button("Odhlásiť sa");
        odhlasBtn.setFont(Font.font("Arial", 16));
        odhlasBtn.setStyle("-fx-background-radius:20; -fx-background-color:#e78942; -fx-text-fill:#fff; -fx-font-weight:bold;");
        odhlasBtn.setMaxWidth(Double.MAX_VALUE);
        odhlasBtn.setPrefHeight(38);
        odhlasBtn.setOnAction(e -> app.logout());

        sidebar.getChildren().addAll(skupinyBtn, praceBtn, kalendarBtn, profilBtn, odhlasBtn);
        setLeft(sidebar);

        // Default: moje skupiny
        setCenter(new MojeSkupinyPane(user, app, userManager));
    }

    // Ak už NotificationBell vie počítať deadliny z kalendára, môžeš vrátiť prázdny zoznam
    // alebo implementovať podobne ako v CalendarPane.getDeadlineNotifications()
    private java.util.List<String> getDeadlineNotifications() {
        return java.util.Collections.emptyList();
    }

    // Getter pre MainApp – aby vedel získať zvonček
    public NotificationBell getNotificationBell() {
        return notificationBell;
    }

    private Button basicNavBtn(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 15));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(36);
        btn.setStyle(
                "-fx-background-radius:16; -fx-background-color:linear-gradient(90deg, #b4845a 80%, #deb079 100%);" +
                        "-fx-text-fill: #fff; -fx-font-weight:bold;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-radius:16; -fx-background-color:#ffd39e;" +
                        "-fx-text-fill: #8a4b13; -fx-font-weight:bold;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-radius:16; -fx-background-color:linear-gradient(90deg, #b4845a 80%, #deb079 100%);" +
                        "-fx-text-fill: #fff; -fx-font-weight:bold;"
        ));
        return btn;
    }
}

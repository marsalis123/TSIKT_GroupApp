package com.example.test_1;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NotificationBell extends StackPane {
    private final Circle bellDot;
    private final Button bellButton;
    private final VBox notifyPane;
    private boolean hasNewNotifications = false;

    // interný zoznam správ (deadliny + real-time notifikácie)
    private final List<String> currentNotifications = new ArrayList<>();

    public NotificationBell(Supplier<List<String>> notificationSupplier) {
        setPadding(new Insets(0, 0, 0, 0));
        setMaxWidth(250);

        bellButton = new Button("🔔");
        bellButton.setFont(Font.font("Arial", 19));
        bellButton.setStyle("-fx-background-radius:24;-fx-background-color:#fff3fc;-fx-effect:dropshadow(gaussian,#ff00e0, 8,0.16,0,0);");
        bellButton.setPrefSize(38, 38);
        bellButton.setFocusTraversable(false);

        bellDot = new Circle(5, Color.web("#ff00e0"));
        bellDot.setTranslateX(12);
        bellDot.setTranslateY(-15);
        bellDot.setVisible(false);

        StackPane bellWrap = new StackPane(bellButton, bellDot);
        bellWrap.setAlignment(Pos.TOP_RIGHT);
        getChildren().add(bellWrap);

        notifyPane = new VBox();
        notifyPane.setAlignment(Pos.TOP_LEFT);
        notifyPane.setStyle("-fx-background-radius:17;-fx-background-color:#f8fff3;-fx-border-color:#ff00e0;-fx-border-width:2;");
        notifyPane.setPadding(new Insets(13));
        notifyPane.setVisible(false);
        notifyPane.setMaxWidth(230);
        notifyPane.setMinWidth(180);
        notifyPane.setMaxHeight(180);
        StackPane.setAlignment(notifyPane, Pos.TOP_RIGHT);
        StackPane.setMargin(notifyPane, new Insets(48, 18, 0, 0)); // pod zvonček vpravo hore
        getChildren().add(notifyPane);

        // klik na zvonček – otvorí panel s aktuálnym zoznamom currentNotifications
        bellButton.setOnAction(e -> openNotifyPaneWithAnimation(() -> currentNotifications));

        // klik mimo panel/zvonček => zavrie
        this.setOnMousePressed(ev -> {
            if (notifyPane.isVisible()
                    && !notifyPane.isHover() && !bellButton.isHover()) {
                notifyPane.setVisible(false);
            }
        });

        // inicializuj notifikácie z dodaného supplieru (deadliny z kalendára atď.)
        currentNotifications.clear();
        currentNotifications.addAll(notificationSupplier.get());
        updateNotifications(currentNotifications);
    }

    private void openNotifyPaneWithAnimation(Supplier<List<String>> notificationSupplier) {
        showNotificationPanel(notificationSupplier);
        notifyPane.setOpacity(0);
        notifyPane.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(260), notifyPane);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), notifyPane);
        tt.setFromY(-18);
        tt.setToY(0);
        fade.play();
        tt.play();
        bellDot.setVisible(false);
        hasNewNotifications = false;
    }

    private void showNotificationPanel(Supplier<List<String>> notificationSupplier) {
        notifyPane.getChildren().clear();
        List<String> notifyEvents = notificationSupplier.get();
        Label hdr = new Label("Oznamy:");
        hdr.setFont(Font.font("Arial", 14));
        hdr.setStyle("-fx-font-weight:bold; -fx-text-fill:#ff00e0;");
        VBox eventsList = new VBox(5);
        for (String s : notifyEvents) {
            Label lbl = new Label("• " + s);
            lbl.setFont(Font.font("Arial", 13));
            eventsList.getChildren().add(lbl);
        }
        if (notifyEvents.isEmpty()) {
            eventsList.getChildren().add(new Label("Žiadne nové upozornenia ☀️"));
        }
        notifyPane.getChildren().addAll(hdr, eventsList);
    }

    // zavolaj vždy po batch-prepočte notifikácií (napr. pri zmene mesiaca v kalendári)
    public void updateNotifications(List<String> notifyEvents) {
        currentNotifications.clear();
        currentNotifications.addAll(notifyEvents);
        boolean hasNoti = !currentNotifications.isEmpty();
        bellDot.setVisible(hasNoti);
        hasNewNotifications = hasNoti;
        if (notifyPane.isVisible()) {
            showNotificationPanel(() -> currentNotifications);
        }
    }

    // zavolá WebSocket klient pri jednej novej udalosti (feed/job/kalendár)
    public void addNotification(String text) {
        currentNotifications.add(0, text); // najnovšia hore
        bellDot.setVisible(true);
        hasNewNotifications = true;
        if (notifyPane.isVisible()) {
            showNotificationPanel(() -> currentNotifications);
        }
    }
}

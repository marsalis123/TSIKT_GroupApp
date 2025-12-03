package com.example.test_1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private UserManager userManager;

    // WebSocket klient pre notifikácie
    private NotificationWebSocketClient notificationClient;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // ak DB ešte používaš lokálne, nechaj; inak môžeš odstrániť
        DBManager.createTables();

        this.userManager = new UserManager();

        primaryStage.setTitle("Aplikácia");
        showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void logout() {
        // prípadné ukončenie WS spojenia (ak v klientovi doplníš close())
        // if (notificationClient != null) notificationClient.close();
        showLoginScreen();
    }

    public void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(this, userManager);
        Scene scene = new Scene(loginScreen.getView(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.show();
    }

    public void showMainMenu(User user) {
        // vytvor hlavné menu
        MainMenu mainMenu = new MainMenu(this, user, userManager);
        Scene scene = new Scene(mainMenu, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);

        // z MainMenu si vyber NotificationBell
        NotificationBell bell = mainMenu.getNotificationBell();

        // WebSocket klient vytvor len raz
        if (notificationClient == null) {
            notificationClient = new NotificationWebSocketClient((type, message, groupId) -> {
                Platform.runLater(() -> {
                    // pridá prijatú správu do zvončeka
                    bell.addNotification(message);
                });
            });
        }
    }

    public void showProfileScreen(User user) {
        ProfileScreen profileScreen = new ProfileScreen(this, user, userManager);
        Scene scene = new Scene(profileScreen.getView(), 500, 450);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
    }
}

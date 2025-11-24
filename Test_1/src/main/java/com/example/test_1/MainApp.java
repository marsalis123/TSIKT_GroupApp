package com.example.test_1;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class MainApp extends Application {

    private Stage primaryStage;
    private UserManager userManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Initialize database tables
        DBManager.createTables();

        this.userManager = new UserManager();

        primaryStage.setTitle("Aplikácia");
        showLoginScreen();
    }
    public void logout() {
        // Zobrazenie login okna pri odhlásení
        showLoginScreen();
    }


    public void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(this, userManager);
        Scene scene = new Scene(loginScreen.getView(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showMainMenu(User user) {
        MainMenu mainMenu = new MainMenu(this, user, userManager);
        Scene scene = new Scene(mainMenu, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
    }



    public void showProfileScreen(User user) {
        ProfileScreen profileScreen = new ProfileScreen(this, user, userManager);
        Scene scene = new Scene(profileScreen.getView(), 500, 450);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);
    }


    public static void main(String[] args) {
        launch(args);
    }
}

package com.example.test_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.Parent;

public class LoginScreen {
    private MainApp app;
    private UserManager userManager;
    private VBox view;

    public LoginScreen(MainApp app, UserManager userManager) {
        this.app = app;
        this.userManager = userManager;
        createView();
    }

    private void createView() {
        Color backgroundStart = Color.web("#ffe0b2");
        Color backgroundEnd = Color.web("#ffcc80");
        Color accent = Color.web("#ff7043");
        Color secondary = Color.web("#ff8a65");
        Color textStrong = Color.web("#d84315");

        view = new VBox(18);
        view.setPadding(new Insets(40));
        view.setAlignment(Pos.CENTER);

        view.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE, new Stop[]{
                        new Stop(0, backgroundStart), new Stop(1, backgroundEnd)
                }),
                CornerRadii.EMPTY, Insets.EMPTY
        )));

        Label titleLabel = new Label("Vitajte!");
        titleLabel.setFont(Font.font("Arial", 32));
        titleLabel.setTextFill(accent);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Používateľské meno");
        usernameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Heslo");
        passwordField.setMaxWidth(250);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 14));
        messageLabel.setTextFill(textStrong);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Prihlásiť sa");
        loginButton.setStyle(
                "-fx-background-radius: 30;" +
                        "-fx-padding: 10 32;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-color: linear-gradient(to right, #ff7043, #ffab91);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: Arial;"
        );

        Button registerButton = new Button("Registrovať sa");
        registerButton.setStyle(
                "-fx-background-radius: 30;" +
                        "-fx-padding: 10 32;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-color: linear-gradient(to right, #ff8a65, #ffcc80);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: Arial;"
        );

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Vyplňte všetky polia!");
                return;
            }

            User user = userManager.login(username, password);
            if (user != null) {
                app.showMainMenu(user);
            } else {
                messageLabel.setText("Nesprávne prihlasovacie údaje!");
            }
        });

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Vyplňte všetky polia!");
                return;
            }

            if (userManager.register(username, password)) {
                messageLabel.setTextFill(accent);
                messageLabel.setText("Registrácia úspešná! Môžete sa prihlásiť.");
                usernameField.clear();
                passwordField.clear();
            } else {
                messageLabel.setTextFill(textStrong);
                messageLabel.setText("Používateľ už existuje!");
            }
        });

        buttonBox.getChildren().addAll(loginButton, registerButton);
        view.getChildren().addAll(titleLabel, usernameField, passwordField, buttonBox, messageLabel);
    }

    public Parent getView() {
        return view;
    }
}

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
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            StringBuilder errors = new StringBuilder();

            if (username.isEmpty()) {
                errors.append("Používateľské meno je povinné.\n");
            } else if (username.length() < 3) {
                errors.append("Používateľské meno musí mať aspoň 3 znaky.\n");
            }

            if (password.isEmpty()) {
                errors.append("Heslo je povinné.\n");
            }

            if (errors.length() > 0) {
                messageLabel.setTextFill(textStrong);
                messageLabel.setText(errors.toString());
                return;
            }

            User user = userManager.login(username, password);
            if (user != null) {
                app.showMainMenu(user);
            } else {
                messageLabel.setTextFill(textStrong);
                messageLabel.setText("Nesprávne prihlasovacie údaje!");
            }
        });


        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            StringBuilder errors = new StringBuilder();

            // username pravidlá
            if (username.isEmpty()) {
                errors.append("Používateľské meno je povinné.\n");
            } else {
                if (username.length() < 3) {
                    errors.append("Používateľské meno musí mať aspoň 3 znaky.\n");
                }
                if (!username.matches("^[A-Za-z0-9_]+$")) {
                    errors.append("Používateľské meno môže obsahovať len písmená, čísla a _.\n");
                }
            }

            // password pravidlá
            if (password.isEmpty()) {
                errors.append("Heslo je povinné.\n");
            } else {
                if (password.length() < 8) {
                    errors.append("Heslo musí mať aspoň 8 znakov.\n");
                }
                if (!password.matches(".*[0-9].*")) {
                    errors.append("Heslo musí obsahovať aspoň jedno číslo.\n");
                }
                if (!password.matches(".*[A-Z].*")) {
                    errors.append("Heslo musí obsahovať aspoň jedno veľké písmeno.\n");
                }
                if (!password.matches(".*[a-z].*")) {
                    errors.append("Heslo musí obsahovať aspoň jedno malé písmeno.\n");
                }
            }

            if (errors.length() > 0) {
                messageLabel.setTextFill(textStrong);
                messageLabel.setText(errors.toString());
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

package com.example.test_1;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.scene.Parent;
import java.io.File;

public class ProfileScreen {
    private MainApp app;
    private User user;
    private UserManager userManager;
    private VBox view;
    private ImageView profilePhotoView;
    private String photoPath;

    public ProfileScreen(MainApp app, User user, UserManager userManager) {
        this.app = app;
        this.user = user;
        this.userManager = userManager;
        this.photoPath = user.getPhotoPath();
        createView();
    }

    private void createView() {
        Color backgroundStart = Color.web("#ffe0b2");
        Color backgroundEnd = Color.web("#ffcc80");
        Color accent = Color.web("#ff7043");
        Color secondary = Color.web("#ff8a65");
        Color textStrong = Color.web("#d84315");

        view = new VBox(18);
        view.setPadding(new Insets(32));
        view.setAlignment(Pos.TOP_CENTER);

        view.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE, new Stop[]{
                        new Stop(0, backgroundStart), new Stop(1, backgroundEnd)
                }),
                CornerRadii.EMPTY, Insets.EMPTY
        )));

        Label titleLabel = new Label("Môj Profil");
        titleLabel.setFont(Font.font("Arial", 28));
        titleLabel.setTextFill(accent);

        profilePhotoView = new ImageView();
        profilePhotoView.setFitHeight(90);
        profilePhotoView.setFitWidth(90);
        profilePhotoView.setPreserveRatio(true);
        profilePhotoView.setSmooth(true);
        profilePhotoView.setStyle("-fx-effect: dropshadow(gaussian, #ff7043, 10, 0.4, 0, 0);");

        showProfilePhoto();

        Button photoButton = new Button("Nahrať fotografiu");
        photoButton.setStyle(
                "-fx-background-radius: 30;" +
                        "-fx-padding: 8 24;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: linear-gradient(to right, #ff8a65, #ffcc80);" +
                        "-fx-text-fill: white;"
        );

        photoButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Vyberte fotografiu");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Obrázky", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(view.getScene().getWindow());
            if (selectedFile != null) {
                photoPath = selectedFile.getAbsolutePath();
                profilePhotoView.setImage(new Image(selectedFile.toURI().toString()));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(16));

        Label nameLabel = new Label("Meno:");
        nameLabel.setTextFill(accent);
        TextField nameField = new TextField(user.getName());
        nameField.setPromptText("Vaše meno");

        Label emailLabel = new Label("Email:");
        emailLabel.setTextFill(accent);
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("váš@email.com");

        Label ageLabel = new Label("Vek:");
        ageLabel.setTextFill(accent);
        TextField ageField = new TextField(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
        ageField.setPromptText("Váš vek");

        Label idLabel = new Label("ID: " + user.getId());
        idLabel.setFont(Font.font("Arial", 14));
        idLabel.setTextFill(Color.web("#d84315"));
        view.getChildren().add(idLabel);


        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(ageLabel, 0, 2);
        grid.add(ageField, 1, 2);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 14));
        messageLabel.setTextFill(textStrong);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Uložiť");
        saveButton.setStyle(
                "-fx-background-radius: 30;" +
                        "-fx-padding: 10 32;" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-color: linear-gradient(to right, #ff7043, #ffab91);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: Arial;"
        );

        Button backButton = new Button("Späť");
        backButton.setStyle(
                "-fx-background-radius: 30;" +
                        "-fx-padding: 10 32;" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-color: linear-gradient(to right, #ff8a65, #ffcc80);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: Arial;"
        );

        saveButton.setOnAction(e -> {
            try {
                user.setName(nameField.getText());
                user.setEmail(emailField.getText());

                String ageText = ageField.getText();
                if (!ageText.isEmpty()) {
                    int age = Integer.parseInt(ageText);
                    if (age > 0 && age < 150) {
                        user.setAge(age);
                    } else {
                        messageLabel.setTextFill(textStrong);
                        messageLabel.setText("Neplatný vek!");
                        return;
                    }
                }

                user.setPhotoPath(photoPath);   // uložíme cestu k fotke
                if (userManager.saveUser(user)) {
                    messageLabel.setTextFill(accent);
                    messageLabel.setText("Informácie boli úspešne uložené do databázy!");
                } else {
                    messageLabel.setTextFill(textStrong);
                    messageLabel.setText("Chyba pri ukladaní do databázy!");
                }
            } catch (NumberFormatException ex) {
                messageLabel.setTextFill(textStrong);
                messageLabel.setText("Vek musí byť číslo!");
            }
        });

        backButton.setOnAction(e -> app.showMainMenu(user));
        buttonBox.getChildren().addAll(saveButton, backButton);
        view.getChildren().addAll(titleLabel, profilePhotoView, photoButton, grid, buttonBox, messageLabel);
    }

    private void showProfilePhoto() {
        if (photoPath != null && !photoPath.isEmpty()) {
            profilePhotoView.setImage(new Image(new File(photoPath).toURI().toString()));
        } else {
            // Default avatar
            profilePhotoView.setImage(new Image("https://ui-avatars.com/api/?name=" + user.getUsername()));
        }
    }

    public Parent getView() {
        return view;
    }
}

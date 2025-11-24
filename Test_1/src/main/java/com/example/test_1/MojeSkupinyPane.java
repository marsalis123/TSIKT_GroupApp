package com.example.test_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class MojeSkupinyPane extends BorderPane {
    private final User user;
    private final MainApp app;
    private final UserManager userManager;
    private List<Group> userGroups;

    public MojeSkupinyPane(User user, MainApp app, UserManager userManager) {
        this.user = user;
        this.app = app;
        this.userManager = userManager;
        setPadding(new Insets(30));
        reload();
    }

    private void reload() {
        setCenter(null);
        setLeft(null);
        userGroups = userManager.getUserGroups(user.getUsername());
        if (userGroups.isEmpty()) {
            showGhostPlus();
        } else {
            showGroupList();
        }
    }

    private void showGhostPlus() {
        StackPane ghostPane = new StackPane();
        ghostPane.setAlignment(Pos.CENTER);

        Label plusIcon = new Label("+");
        plusIcon.setFont(Font.font("Arial", 100));
        plusIcon.setTextFill(Color.rgb(255,128,64, 0.33));
        plusIcon.setStyle("-fx-font-weight: bold;");

        Label hint = new Label("Pridať svoju prvú skupinu");
        hint.setFont(Font.font("Arial", 23));
        hint.setTextFill(Color.web("#bb4d00"));
        hint.setStyle("-fx-background-color:rgba(255,238,204,0.35); -fx-background-radius:12; -fx-padding:16 40;");

        VBox v = new VBox(plusIcon, hint);
        v.setSpacing(18);
        v.setAlignment(Pos.CENTER);

        ghostPane.getChildren().add(v);

        plusIcon.setOnMouseClicked(e -> showCreateGroupDialog());

        setCenter(ghostPane);
    }

    private void showGroupList() {
        VBox listBox = new VBox(12);
        listBox.setAlignment(Pos.CENTER);
        listBox.setPadding(new Insets(32));
        listBox.setPrefWidth(530);
        listBox.setStyle("-fx-background-color: rgba(255, 132, 82, 0.23);"
                + "-fx-background-radius: 45;"
                + "-fx-effect: dropshadow(gaussian, #ffd19c, 24, 0.14, 0, 8);");

        Label groupsTitle = new Label("Moje skupiny");
        groupsTitle.setFont(Font.font("Arial", 27));
        groupsTitle.setTextFill(Color.web("#d84315"));
        groupsTitle.setStyle("-fx-font-weight: bold; -fx-padding:0 0 18 0;");

        ListView<Group> groupListView = new ListView<>();
        groupListView.getItems().addAll(userGroups);
        groupListView.setStyle("-fx-background-radius:34; -fx-background-color:rgba(255,255,255,0.38)");
        groupListView.setPrefHeight(340);

        listBox.getChildren().addAll(groupsTitle, groupListView);
        setCenter(listBox);

        groupListView.setOnMouseClicked(e -> {
            Group selected = groupListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showGroupDetail(selected);
            }
        });
    }

    private void showGroupDetail(Group group) {
        setCenter(null);
        setLeft(null);

        // ----- HEAD ---------
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(group.name);
        nameLabel.setFont(Font.font("Arial", 34));
        nameLabel.setTextFill(Color.web("#d84315"));
        nameLabel.setStyle("-fx-font-weight:bold;");
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Button groupDelBtn = new Button("Zmazať skupinu");
        groupDelBtn.setFont(Font.font("Arial", 14));
        groupDelBtn.setStyle("-fx-background-radius:18; -fx-background-color:#ffd19c; -fx-text-fill:#700000;");
        groupDelBtn.setVisible(group.owner.equals(user.getUsername()));
        groupDelBtn.setOnAction(e -> {
            userManager.deleteGroup(group.id);
            reload();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(nameLabel, spacer, groupDelBtn);

        HBox mainContent = new HBox(24);
        mainContent.setAlignment(Pos.TOP_CENTER);

        VBox feedPane = new VBox(14);
        feedPane.setStyle("-fx-background-radius:38; -fx-background-color:rgba(255,255,255,0.82); -fx-effect: dropshadow(gaussian, #ffcc80, 8,0.10,0,4);");
        feedPane.setPadding(new Insets(22));
        feedPane.setPrefWidth(480);
        feedPane.setMinWidth(340);
        HBox.setHgrow(feedPane, Priority.ALWAYS);

        VBox membersPane = new VBox(10);
        membersPane.setStyle("-fx-background-radius:20; -fx-background-color:rgba(255,229,185,0.7);");
        membersPane.setPadding(new Insets(14));
        membersPane.setMinWidth(190);
        membersPane.setMaxWidth(320);
        HBox.setHgrow(membersPane, Priority.SOMETIMES);

        // ------ FEED PANEL PREPINAČ ------
        final String[] feedMode = {"list"}; // "list", "editor", "detail"
        final FeedMessage[] detailMsg = {null};
        final Runnable[] switcher = new Runnable[1];

        switcher[0] = () -> {
            feedPane.getChildren().clear();

            if ("editor".equals(feedMode[0])) {
                // Nový editor priamo v paneli
                Label t1 = new Label("Názov správy");
                TextField msgTitle = new TextField();
                Label t2 = new Label("Obsah správy");
                TextArea msgArea = new TextArea();
                msgArea.setPrefRowCount(6);
                Label pdfLabel = new Label("Priložené PDF: žiadny súbor");
                Button uploadBtn = new Button("Nahrať PDF");
                final String[] uploadedPath = {null};
                uploadBtn.setOnAction(e -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                    File selected = fileChooser.showOpenDialog(getScene().getWindow());
                    if (selected != null) {
                        uploadedPath[0] = selected.getAbsolutePath();
                        pdfLabel.setText("Priložené PDF: " + selected.getName());
                    }
                });
                Button submitBtn = new Button("Pridať správu do feedu");
                submitBtn.setStyle("-fx-background-radius:18; -fx-background-color:#ffcc80; -fx-text-fill:#131313;");
                submitBtn.setOnAction(ev -> {
                    String title = msgTitle.getText().trim();
                    String content = msgArea.getText().trim();
                    String pdfPath = uploadedPath[0];
                    if (!title.isEmpty()) {
                        FeedMessage msg = new FeedMessage(-1, group.id, title, content, pdfPath, user.getId(), "");
                        userManager.addFeedMessage(msg);
                        feedMode[0] = "list";
                        switcher[0].run();
                    }
                });
                Button cancelBtn = new Button("Zrušiť");
                cancelBtn.setStyle("-fx-background-radius:18; -fx-background-color:#FAD4DB;");
                cancelBtn.setOnAction(ev -> {
                    feedMode[0] = "list";
                    switcher[0].run();
                });
                VBox editorBox = new VBox(10, t1, msgTitle, t2, msgArea, uploadBtn, pdfLabel, new HBox(12, submitBtn, cancelBtn));
                editorBox.setPadding(new Insets(14));
                editorBox.setStyle("-fx-background-radius:28; -fx-background-color:rgba(255,255,255,0.83);");
                feedPane.getChildren().add(editorBox);
            } else if ("detail".equals(feedMode[0]) && detailMsg[0] != null) {
                FeedMessage msg = detailMsg[0];
                Label title = new Label(msg.title);
                title.setFont(Font.font("Arial", 21));
                title.setStyle("-fx-font-weight: bold");
                Label content = new Label(msg.content != null ? msg.content : "(Žiadny text)");
                content.setWrapText(true);

                VBox v = new VBox(13, title, content);
                v.setPadding(new Insets(5, 0, 12, 0));

                if (msg.pdfPath != null && !msg.pdfPath.isEmpty()) {
                    Hyperlink pdfLink = new Hyperlink("Stiahnuť PDF");
                    pdfLink.setOnAction(e -> {
                        try {
                            java.awt.Desktop.getDesktop().open(new File(msg.pdfPath));
                        } catch (Exception ex) {
                            Alert a = new Alert(Alert.AlertType.ERROR, "PDF sa nepodarilo otvoriť.");
                            a.showAndWait();
                        }
                    });
                    v.getChildren().add(pdfLink);
                }

                Button backBtn = new Button("Späť na feed");
                backBtn.setStyle("-fx-background-radius:16; -fx-background-color:#ffd19c;");
                backBtn.setOnAction(ev -> {
                    feedMode[0] = "list";
                    switcher[0].run();
                });

                feedPane.getChildren().addAll(v, backBtn);
            } else { // list správ
                Button addMsgBtn = new Button("+");
                addMsgBtn.setFont(Font.font("Arial", 20));
                addMsgBtn.setStyle("-fx-background-radius:22; -fx-background-color:#ffd19c; -fx-text-fill:#d84315;");
                HBox plusH = new HBox(addMsgBtn);
                plusH.setAlignment(Pos.CENTER_LEFT);
                addMsgBtn.setOnAction(ev -> {
                    feedMode[0] = "editor";
                    switcher[0].run();
                });
                List<FeedMessage> messages = userManager.getFeedMessages(group.id);
                VBox msgListBox = new VBox(14);
                msgListBox.setAlignment(Pos.TOP_CENTER);
                msgListBox.setFillWidth(true);
                for (FeedMessage msg : messages) {
                    Button msgBtn = new Button(msg.title);
                    msgBtn.setFont(Font.font("Arial", 16));
                    msgBtn.setPrefHeight(56); msgBtn.setMinHeight(56); msgBtn.setMaxHeight(56);
                    msgBtn.setMaxWidth(Double.MAX_VALUE); msgBtn.setPrefWidth(Double.MAX_VALUE);
                    msgBtn.setStyle("-fx-background-radius:22; -fx-background-color:rgba(255,255,255,0.95); -fx-text-fill:#d84315; -fx-font-size: 16px; -fx-alignment:CENTER_LEFT;");
                    msgBtn.setOnAction(ev2 -> {
                        feedMode[0] = "detail";
                        detailMsg[0] = msg;
                        switcher[0].run();
                    });
                    msgListBox.getChildren().add(msgBtn);
                }
                ScrollPane msgScroll = new ScrollPane(msgListBox);
                msgScroll.setFitToWidth(true);
                msgScroll.setStyle("-fx-background: transparent;");
                VBox.setVgrow(msgScroll, Priority.ALWAYS);
                feedPane.getChildren().addAll(plusH, msgScroll);
            }
        };
        switcher[0].run();

        // ----------- MEMBERS ------------------------
        Label memTitle = new Label("Členovia");
        memTitle.setFont(Font.font("Arial", 15));
        memTitle.setTextFill(Color.web("#d84315"));

        ScrollPane membersScroll = new ScrollPane();
        VBox membersListBox = new VBox(8);
        membersListBox.setAlignment(Pos.TOP_LEFT);
        membersScroll.setContent(membersListBox);
        membersScroll.setFitToWidth(true);
        membersScroll.setStyle("-fx-background: transparent;");
        VBox.setVgrow(membersScroll, Priority.ALWAYS);

        List<User> members = userManager.getGroupMembers(group.id);
        for (User member : members) {
            HBox memberRow = new HBox(7);
            memberRow.setAlignment(Pos.CENTER_LEFT);
            Label memberLabel = new Label(
                    member.getName() + " (" + member.getUsername() + ", ID: " + member.getId() + ")"
            );
            memberLabel.setFont(Font.font("Arial", 13));
            memberRow.getChildren().add(memberLabel);
            if (group.owner.equals(user.getUsername()) && member.getId() != user.getId()) {
                Button delBtn = new Button("Odstrániť");
                delBtn.setStyle("-fx-background-radius:10; -fx-background-color:#ffccbc; -fx-font-size:11px;");
                delBtn.setOnAction(ev -> {
                    userManager.removeUserFromGroup(group.id, member.getId());
                    showGroupDetail(group);
                });
                memberRow.getChildren().add(delBtn);
            }
            membersListBox.getChildren().add(memberRow);
        }
        if (group.owner.equals(user.getUsername())) {
            Label addLabel = new Label("Pridať člena:");
            TextField addField = new TextField();
            addField.setPromptText("Meno, Email alebo ID");
            addField.setMaxWidth(120);

            Button addBtn = new Button("➕");
            addBtn.setStyle("-fx-background-radius:12; -fx-background-color:#ffd19c;");
            addBtn.setOnAction(e -> {
                String input = addField.getText().trim();
                if (input.isEmpty()) return;
                User found = userManager.findUser(input);
                if (found != null) {
                    if (userManager.addUserToGroup(group.id, found.getId())) {
                        showGroupDetail(group);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Používateľ už je v skupine.");
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Používateľ nenájdený!");
                    alert.showAndWait();
                }
                addField.clear();
            });

            HBox addBox = new HBox(8, addField, addBtn);
            addBox.setAlignment(Pos.CENTER_LEFT);
            membersPane.getChildren().addAll(memTitle, membersScroll, addLabel, addBox);
        } else {
            membersPane.getChildren().addAll(memTitle, membersScroll);
        }

        mainContent.getChildren().addAll(feedPane, membersPane);
        setTop(header);
        setCenter(mainContent);
    }

    private void showCreateGroupDialog() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Nová skupina");
        dialog.setHeaderText("Zadaj názov skupiny:");
        dialog.setContentText("Názov skupiny:");

        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                Group g = new Group(trimmed, user.getUsername());
                userManager.addGroup(g);
                reload();
            }
        });
    }
}

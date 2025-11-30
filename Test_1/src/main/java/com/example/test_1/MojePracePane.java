package com.example.test_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.*;

public class MojePracePane extends VBox {
    private final User user;
    private final UserManager userManager;
    private final List<Group> userGroups;
    private List<Job> jobs;
    private VBox mainPane;

    // na rozbaľovanie logov
    private final Set<Integer> expandedLogs = new HashSet<>();
    private String mode = "list";
    private Job selectedJob = null;

    public MojePracePane(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
        this.userGroups = userManager.getUserGroups(user.getUsername());
        setPadding(new Insets(18,12,14,22));
        setSpacing(0);
        setStyle("-fx-background-color: transparent;");
        createLayout();
    }

    private void createLayout() {
        getChildren().clear();
        Label header = new Label("Moje práce");
        header.setFont(Font.font("Arial", 31));
        header.setTextFill(Color.web("#d84315"));

        Button addBtn = new Button("+");
        addBtn.setFont(Font.font(18));
        addBtn.setStyle("-fx-background-radius:24; -fx-background-color:#ff9800; -fx-text-fill:#fff;");
        addBtn.setOnAction(e -> showJobEditor(null));

        HBox topBar = new HBox(header, addBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(24);

        mainPane = new VBox();
        VBox.setVgrow(mainPane, Priority.ALWAYS);

        getChildren().addAll(topBar, mainPane);
        showJobList();
    }

    private void showJobList() {
        mode = "list";
        selectedJob = null;
        mainPane.getChildren().clear();
        jobs = userManager.getUserJobs(user.getId());
        VBox jobListBox = new VBox(13);
        jobListBox.setPadding(new Insets(18,0,0,0));
        for (Job job : jobs) {
            HBox row = new HBox(18);
            Label title = new Label(job.title);
            title.setFont(Font.font("Arial", 17));
            title.setMaxWidth(210);
            title.setMinWidth(120);

            Label groupLabel = new Label(getGroupName(job.groupId));
            groupLabel.setFont(Font.font("Arial", 13));
            groupLabel.setStyle("-fx-background-color: #ffe0b2; -fx-background-radius:8; -fx-padding:2 16; -fx-text-fill:#bb4d00;");

            Label status = new Label(job.status);
            status.setFont(Font.font("Arial", 12));
            status.setStyle("-fx-background-radius:8; -fx-padding:2 13; " +
                    (job.status.equals("DONE") ? "-fx-background-color:#a5d6a7;" :
                            job.status.equals("IN PROGRESS") ? "-fx-background-color:#fff59d;" :
                                    "-fx-background-color:#ffccbc;") +
                    "-fx-text-fill:#333;"
            );

            Button edit = new Button("Upraviť");
            edit.setStyle("-fx-background-radius:18; -fx-background-color:#dabfff;");
            edit.setOnAction(e -> showJobEditor(job));

            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(title, groupLabel, status, edit);
            row.setOnMouseClicked(evt -> showJobDetail(job));
            jobListBox.getChildren().add(row);
        }
        ScrollPane sp = new ScrollPane(jobListBox);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        mainPane.getChildren().add(sp);
    }

    private String getGroupName(int groupId) {
        for (Group g: userGroups) if (g.id == groupId) return g.name;
        return "Neznáma";
    }

    private void showJobDetail(Job job) {
        mode = "detail";
        selectedJob = job;
        mainPane.getChildren().clear();

        GridPane rootGrid = new GridPane();
        rootGrid.setHgap(32);
        rootGrid.setVgap(10);
        rootGrid.setPadding(new Insets(16, 12, 16, 12));

        // Ľavý panel – popis práce + editor
        VBox leftCol = new VBox(22);

        VBox infoBox = new VBox(7);
        infoBox.getChildren().addAll(
                infoLine("Popis:", job.description),
                infoLine("Skupina:", getGroupName(job.groupId)),
                infoLine("Stav:", job.status),
                infoLine("Vytvorené:", job.createdAt != null ? job.createdAt : "")
        );

        Label workLabel = new Label("Úprava textu / nový prídavok k práci:");
        TextArea workArea = new TextArea();
        workArea.setPromptText("Sem napíš nový obsah, doplnenie, aktualizáciu s formátovaním");
        workArea.setPrefRowCount(7);
        workArea.setFont(Font.font("Arial", 15));
        workArea.setStyle("-fx-background-radius:8; -fx-background-color:#fffbe3;");

        Label commitLabel = new Label("Komentár / commit:");
        TextField commitField = new TextField();
        commitField.setPromptText("Krátky popis, čo sa menilo...");

        Label pdfLabel = new Label("Priložené PDF: žiadny súbor");
        Button uploadBtn = new Button("Nahrať PDF");
        final String[] uploaded = {null};
        uploadBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            java.io.File f = fc.showOpenDialog(getScene().getWindow());
            if (f != null) {
                uploaded[0] = f.getAbsolutePath();
                pdfLabel.setText("Priložené PDF: " + f.getName());
            }
        });

        Button submitBtn = new Button("Uložiť zmenu / commit");
        submitBtn.setStyle("-fx-background-radius:14; -fx-background-color:#ff9800; -fx-text-fill:#fff;");
        submitBtn.setOnAction(ev -> {
            String workTxt = workArea.getText().trim();
            String commitTxt = commitField.getText().trim();
            String pdf = uploaded[0];
            if (workTxt.isEmpty() && commitTxt.isEmpty() && pdf==null) return;
            userManager.addJobLog(new JobLog(-1, job.id, user.getId(), workTxt, commitTxt, pdf, "", user.getName()));
            showJobDetail(job);
        });

        Button backBtn = new Button("Späť na zoznam");
        backBtn.setStyle("-fx-background-radius:14; -fx-background-color:#dea477; -fx-text-fill:#fff;");
        backBtn.setOnAction(ev -> showJobList());

        // NOVÉ TLAČIDLO ŠTATISTIKY
        Button statsBtn = new Button("Štatistiky");
        statsBtn.setStyle("-fx-background-radius:14; -fx-background-color:#8bc34a; -fx-text-fill:#fff;");
        statsBtn.setOnAction(ev -> showJobStats(job));

        HBox logButtons = new HBox(10, submitBtn, backBtn, statsBtn);
        logButtons.setAlignment(Pos.CENTER_LEFT);

        VBox editorBox = new VBox(12, workLabel, workArea, commitLabel, commitField, uploadBtn, pdfLabel, logButtons);
        editorBox.setPadding(new Insets(14, 2, 4, 0));

        leftCol.getChildren().addAll(infoBox, editorBox);

        GridPane.setVgrow(leftCol, Priority.ALWAYS);
        rootGrid.add(leftCol, 0, 0);

        // Pravý panel – logy
        VBox logCol = new VBox(12);
        Label logsHdr = new Label("Logy práce / úpravy:");
        logsHdr.setFont(Font.font("Arial", 16));
        logsHdr.setStyle("-fx-font-weight:bold; -fx-text-fill:#b76013;");

        VBox logsVBox = new VBox(18);
        logsVBox.setPadding(new Insets(10,8,10,8));

        List<JobLog> logs = userManager.getJobLogs(job.id);
        for (JobLog log : logs) {
            boolean expanded = expandedLogs.contains(log.id);
            VBox logCard = new VBox(10);
            logCard.setPadding(new Insets(9,11,9,11));
            logCard.setMinWidth(210);
            logCard.setMaxWidth(580);
            logCard.setStyle("-fx-background-radius:18; -fx-background-color:#ffefdf; -fx-effect: dropshadow(gaussian, #e5b687, 6,0.10,0,2);");
            logCard.setAlignment(Pos.TOP_LEFT);

            Label head = new Label(log.authorName + " · " + log.createdAt.substring(0,16));
            head.setFont(Font.font("Arial", 12));
            head.setStyle("-fx-font-weight:bold; -fx-text-fill:#ad5f1c;");

            String display = log.workText == null ? "" :
                    expanded ? log.workText :
                            (log.workText.length() > 90 ? log.workText.substring(0,87) + "..." : log.workText);

            Label workText = new Label(display);
            workText.setWrapText(true);
            workText.setFont(Font.font("Arial", 14));

            Button expandBtn = new Button(expanded ? "Zbaliť" : "Zobraziť");
            expandBtn.setStyle("-fx-background-radius:13; -fx-background-color:#fbc02d; -fx-text-fill:#6d3d1b;");
            expandBtn.setOnAction(e -> {
                if (expanded) expandedLogs.remove(log.id);
                else expandedLogs.add(log.id);
                showJobDetail(job);
            });

            logCard.getChildren().clear();
            logCard.getChildren().addAll(head, workText, expandBtn);

            if (expanded) {
                if (log.commitMsg != null && !log.commitMsg.isEmpty()) {
                    Label cmt = new Label("Komentár: " + log.commitMsg);
                    cmt.setFont(Font.font("Arial", 13));
                    logCard.getChildren().add(cmt);
                }
                if (log.pdfPath != null && !log.pdfPath.isEmpty()) {
                    Hyperlink pdfLink = new Hyperlink("PDF");
                    pdfLink.setOnAction(ev -> {
                        try { java.awt.Desktop.getDesktop().open(new java.io.File(log.pdfPath)); }
                        catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "PDF nedostupné.").showAndWait(); }
                    });
                    logCard.getChildren().add(pdfLink);
                }
            }
            if (!expanded) {
                logCard.setMinHeight(120);
                logCard.setMaxHeight(120);
            } else {
                logCard.setMinHeight(Region.USE_COMPUTED_SIZE);
                logCard.setMaxHeight(Double.MAX_VALUE);
            }

            logsVBox.getChildren().add(logCard);
        }
        ScrollPane logsScroll = new ScrollPane(logsVBox);
        logsScroll.setFitToWidth(true);
        logsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        logsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logsScroll.setPrefViewportHeight(540);

        logCol.getChildren().addAll(logsHdr, logsScroll);
        GridPane.setVgrow(logCol, Priority.ALWAYS);
        rootGrid.add(logCol, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(42);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(58);
        rootGrid.getColumnConstraints().addAll(col1, col2);

        mainPane.getChildren().setAll(rootGrid);
    }

    // ŠTATISTIKY práce
    private void showJobStats(Job job) {
        List<JobLog> logs = userManager.getJobLogs(job.id);
        if (logs.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "K tejto práci zatiaľ nie sú žiadne záznamy.").showAndWait();
            return;
        }

        // 1) Aggregate "work" by user
        Map<String, Integer> workByUser = new LinkedHashMap<>();
        for (JobLog log : logs) {
            String name = log.authorName != null ? log.authorName : ("ID " + log.userId);
            int weight = 0;
            if (log.workText != null) weight += log.workText.length();
            if (log.commitMsg != null) weight += log.commitMsg.length() * 2;
            if (weight == 0) weight = 10;
            workByUser.merge(name, weight, Integer::sum);
        }

        int total = workByUser.values().stream().mapToInt(i -> i).sum();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Štatistiky práce");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        Label title = new Label("Rozdelenie práce pre: " + job.title);
        title.setFont(Font.font("Arial", 16));
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #5d4037;");

        VBox barsBox = new VBox(8);

        String[] colors = {"#ff7043","#42a5f5","#66bb6a","#ffca28","#ab47bc","#26a69a","#ef5350","#8d6e63"};
        int idx = 0;

        double maxWidth = 220; // 100 % šírka

        for (Map.Entry<String,Integer> entry : workByUser.entrySet()) {
            String name = entry.getKey();
            int value = entry.getValue();
            double percent = (total == 0) ? 0 : (value * 100.0 / total);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLbl = new Label(name + String.format(" – %.1f%%", percent));
            nameLbl.setPrefWidth(170);
            nameLbl.setFont(Font.font("Arial", 13));

            // Pozadie
            StackPane barBg = new StackPane();
            barBg.setMinHeight(16);
            barBg.setPrefWidth(maxWidth);
            barBg.setMaxWidth(maxWidth);
            barBg.setStyle("-fx-background-color:#f5f5f5; -fx-background-radius:8;");

            // Farebný bar – dĺžka podľa percent
            Region barFill = new Region();
            double w = maxWidth * (percent / 100.0);
            barFill.setMinWidth(0);
            barFill.setPrefWidth(w);
            barFill.setMaxWidth(Region.USE_PREF_SIZE); // DÔLEŽITÉ – aby sa neroztiahol na celé

            barFill.setMinHeight(16);
            String color = colors[idx % colors.length];
            idx++;
            barFill.setStyle("-fx-background-radius:8; -fx-background-color:" + color + ";");

            barBg.getChildren().add(barFill);
            StackPane.setAlignment(barFill, Pos.CENTER_LEFT);

            row.getChildren().addAll(nameLbl, barBg);
            barsBox.getChildren().add(row);
        }


        Label note = new Label("Odhad je vypočítaný z dĺžky textu logov a komentárov.");
        note.setFont(Font.font("Arial", 11));
        note.setStyle("-fx-text-fill:#757575;");

        content.getChildren().addAll(title, new Separator(), barsBox, new Separator(), note);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showJobEditor(Job job) {
        mode = "editor";
        selectedJob = job;
        mainPane.getChildren().clear();
        VBox box = new VBox(14);
        box.setPadding(new Insets(18,12,14,18));

        Label titleL = new Label((job==null ? "Nová" : "Upraviť") + " práca");
        titleL.setFont(Font.font("Arial", 20));
        titleL.setStyle("-fx-text-fill:#ba6e07; -fx-font-weight:bold;");
        box.getChildren().add(titleL);

        TextField titleField = new TextField(job != null ? job.title : "");
        titleField.setPromptText("Názov");

        TextArea descField = new TextArea(job != null ? job.description : "");
        descField.setPromptText("Popis");

        ComboBox<Group> groupCombo = new ComboBox<>();
        groupCombo.getItems().addAll(userGroups);
        if (job != null) {
            for (Group g: userGroups) if (g.id == job.groupId) groupCombo.getSelectionModel().select(g);
        }

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("OPEN", "IN PROGRESS", "DONE");
        statusCombo.setValue(job != null ? job.status : "OPEN");

        Button saveBtn = new Button(job==null?"Pridať prácu":"Uložiť zmeny");
        saveBtn.setStyle("-fx-background-radius:14; -fx-background-color:#ff9800; -fx-text-fill:#fff;");
        saveBtn.setOnAction(e -> {
            Group selectedGroup = groupCombo.getValue();
            if (titleField.getText().trim().isEmpty() || selectedGroup == null) return;
            if (job == null) {
                userManager.addJob(new Job(-1, titleField.getText().trim(), descField.getText(), selectedGroup.id,
                        statusCombo.getValue(), user.getId(), user.getId(), ""));
            } else {
                userManager.updateJob(job.id, titleField.getText().trim(), descField.getText(),
                        selectedGroup.id, statusCombo.getValue(), user.getId());
            }
            showJobList();
        });

        Button cancelBtn = new Button("Zrušiť");
        cancelBtn.setOnAction(e -> showJobList());

        HBox btns = new HBox(14, saveBtn, cancelBtn);

        box.getChildren().addAll(
                new Label("Názov práce:"), titleField,
                new Label("Popis:"), descField,
                new Label("Skupina:"), groupCombo,
                new Label("Stav:"), statusCombo,
                btns
        );
        mainPane.getChildren().add(box);
    }

    private HBox infoLine(String key, String value) {
        Label k = new Label(key);
        k.setFont(Font.font("Arial", 13));
        k.setStyle("-fx-font-weight:bold; -fx-text-fill:#996626;");
        Label v = new Label(value==null?"":value);
        v.setFont(Font.font("Arial", 13));
        return new HBox(6, k, v);
    }
}

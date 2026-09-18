package application;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.zeromq.ZMQ;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Publisher extends Application {

    private static final String BG       = "#0a0c12";
    private static final String SURFACE  = "#12151f";
    private static final String SURFACE2 = "#1a1e2b";
    private static final String SURFACE3 = "#232838";
    private static final String BORDER   = "#2a3142";
    private static final String TEXT     = "#eaeef7";
    private static final String MUTED    = "#8b93a7";
    private static final String DIM      = "#565e72";
    private static final String ACCENT   = "#8b5cf6";
    private static final String ACCENT2  = "#6366f1";
    private static final String SUCCESS  = "#22c55e";
    private static final String WARNING  = "#f59e0b";
    private static final String DANGER   = "#ef4444";

    private static final String UI   = "Segoe UI";
    private static final String MONO = "'Consolas','Menlo','Monospaced',monospace";

    private static final Map<String, String> TOPIC_COLORS = new LinkedHashMap<>() {{
        put("COURSE", "#8b5cf6");
        put("EXAM",   "#f59e0b");
        put("EVENT",  "#06b6d4");
        put("NEWS",   "#22c55e");
    }};

    private static final Map<String, String> TEMPLATES = new LinkedHashMap<>() {{
        put("New assignment posted",     "New assignment has been posted.");
        put("Midterm exam Monday",       "Midterm exam is on Monday.");
        put("Seminar at 12:00",          "Seminar starts at 12:00.");
        put("Final exam schedule",       "Final exam schedule will be announced next week.");
        put("Guest lecture",             "Guest lecture this Thursday at 10:00 AM.");
        put("Registration open",         "Course registration is now open.");
    }};

    private ZMQ.Context context;
    private ZMQ.Socket  publisher;

    private TextField     customTopic;
    private TextArea      messageArea;
    private TextArea      logArea;
    private TextField     searchField;
    private Label         statusText;
    private Label         counterLabel;
    private Label         perTopicLabel;
    private Circle        statusDot;
    private ToggleGroup   chipGroup;
    private ComboBox<String> priorityBox;
    private ComboBox<String> templateBox;
    private CheckBox      autoClearBox;
    private CheckBox      timestampBox;

    private final List<String>         logHistory  = new ArrayList<>();
    private final Map<String, Integer> topicCounts = new LinkedHashMap<>();
    private int                        totalCount  = 0;

    @Override
    public void start(Stage stage) {
        context   = ZMQ.context(1);
        publisher = context.socket(ZMQ.PUB);
        publisher.bind("tcp://*:5555");

        double screenW = Screen.getPrimary().getVisualBounds().getWidth();
        double screenH = Screen.getPrimary().getVisualBounds().getHeight();
        double w = Math.min(680, screenW - 80);
        double h = Math.min(760, screenH - 80);

        Scene scene = new Scene(buildUI(stage), w, h);
        scene.getStylesheets().add(inlineCss());

        stage.setTitle("ZeroMQ  ·  Publisher Pro");
        stage.setMinWidth(500);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.centerOnScreen();
        stage.show();

        Platform.runLater(messageArea::requestFocus);
    }

    private Parent buildUI(Stage stage) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + BG + ";");
        root.getChildren().addAll(
                buildHeader(stage),
                buildDivider(),
                buildComposer(stage),
                buildStats(),
                buildActivity(),
                buildFooter());
        VBox.setVgrow(root.getChildren().get(4), Priority.ALWAYS);
        return root;
    }

    private HBox buildHeader(Stage stage) {
        statusDot = new Circle(5, Color.web(SUCCESS));
        pulse(statusDot);

        Label brand = new Label("Publisher Pro");
        brand.setFont(Font.font(UI, FontWeight.BOLD, 22));
        brand.setTextFill(Color.web(TEXT));

        HBox title = new HBox(8, statusDot, brand);
        title.setAlignment(Pos.CENTER_LEFT);

        statusText = new Label("Bound to tcp://*:5555   ·   Listening for subscribers");
        statusText.setFont(Font.font(UI, 11.5));
        statusText.setTextFill(Color.web(MUTED));

        VBox left = new VBox(4, title, statusText);
        left.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeBtn = pill("◐  Theme");
        themeBtn.setOnAction(e -> info("Theme", "Dark mode is already the premium look ✨"));

        Button helpBtn = pill("?  Shortcuts");
        helpBtn.setOnAction(e -> info("Keyboard shortcuts",
                "Ctrl + Enter   →  Publish message\nCtrl + L        →  Clear activity\nCtrl + F        →  Focus search\nCtrl + E        →  Export log"));

        HBox right = new HBox(8, themeBtn, helpBtn);
        right.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(left, spacer, right);
        header.setPadding(new Insets(18, 22, 14, 22));
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildComposer(Stage stage) {
        Label topicLbl = sectionLabel("SELECT TOPIC");

        chipGroup = new ToggleGroup();
        FlowPane chips = new FlowPane(8, 8);
        for (String t : TOPIC_COLORS.keySet()) chips.getChildren().add(makeChip(t));
        ((ToggleButton) chips.getChildren().get(0)).setSelected(true);

        customTopic = new TextField();
        customTopic.setPromptText("Or type a custom topic…");
        customTopic.setPrefHeight(34);
        styleField(customTopic);
        customTopic.setOnAction(e -> publish());

        Label msgLbl = sectionLabel("MESSAGE");
        messageArea = new TextArea();
        messageArea.setPromptText("Write your message…  (Ctrl+Enter to send)");
        messageArea.setPrefRowCount(3);
        messageArea.setWrapText(true);
        styleField(messageArea);
        messageArea.textProperty().addListener((o, ov, nv) -> {
            if (nv.length() > 220) messageArea.setText(nv.substring(0, 220));
        });
        messageArea.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER && e.isControlDown()) publish();
        });

        Label priorityLbl = sectionLabel("PRIORITY");
        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Normal", "High", "Urgent");
        priorityBox.setValue("Normal");
        priorityBox.setPrefHeight(34);
        priorityBox.setMaxWidth(Double.MAX_VALUE);
        styleField(priorityBox);

        Label templateLbl = sectionLabel("QUICK TEMPLATES");
        templateBox = new ComboBox<>();
        templateBox.getItems().addAll(TEMPLATES.keySet());
        templateBox.setPromptText("Pick a ready message…");
        templateBox.setPrefHeight(34);
        templateBox.setMaxWidth(Double.MAX_VALUE);
        styleField(templateBox);
        templateBox.valueProperty().addListener((o, ov, nv) -> {
            if (nv != null) messageArea.setText(TEMPLATES.get(nv));
        });

        HBox metaRow = new HBox(12, vbox(4, priorityLbl, priorityBox), vbox(4, templateLbl, templateBox));
        HBox.setHgrow(metaRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(metaRow.getChildren().get(1), Priority.ALWAYS);

        Button publishBtn = new Button("  Publish Message   →  ");
        publishBtn.setMaxWidth(Double.MAX_VALUE);
        publishBtn.setPrefHeight(42);
        publishBtn.setFont(Font.font(UI, FontWeight.BOLD, 14));
        publishBtn.setTextFill(Color.WHITE);
        publishBtn.setCursor(Cursor.HAND);
        publishBtn.setStyle(gradient(ACCENT2, ACCENT));
        publishBtn.setOnMouseEntered(e -> publishBtn.setStyle(
                gradient("#7c6ff2", "#a578ff") +
                "-fx-effect: dropshadow(gaussian, " + alpha(ACCENT, 0.55) + ", 22, 0, 0, 6);"));
        publishBtn.setOnMouseExited(e -> publishBtn.setStyle(gradient(ACCENT2, ACCENT)));
        publishBtn.setOnAction(e -> publish());

        VBox card = new VBox(6,
                topicLbl, chips,
                gap(2), customTopic,
                gap(8),
                msgLbl, messageArea,
                gap(4),
                metaRow,
                gap(6),
                publishBtn);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());
        VBox.setMargin(card, new Insets(12, 20, 8, 20));
        return card;
    }

    private VBox buildStats() {
        Label title = sectionLabel("STATISTICS");

        for (String t : TOPIC_COLORS.keySet()) topicCounts.put(t, 0);

        counterLabel = new Label("0");
        counterLabel.setFont(Font.font(UI, FontWeight.BOLD, 20));
        counterLabel.setTextFill(Color.web(ACCENT));

        Label totalLbl = new Label("TOTAL");
        totalLbl.setFont(Font.font(UI, FontWeight.BOLD, 10));
        totalLbl.setTextFill(Color.web(DIM));

        VBox total = new VBox(2, counterLabel, totalLbl);
        total.setAlignment(Pos.CENTER);
        total.setPadding(new Insets(8, 12, 8, 12));
        total.setStyle(statCard(ACCENT));

        HBox stats = new HBox(8, total);
        stats.setAlignment(Pos.CENTER_LEFT);

        Label perTopicTitle = sectionLabel("PER TOPIC");
        perTopicLabel = new Label("No messages yet");
        perTopicLabel.setFont(Font.font(MONO, 11));
        perTopicLabel.setTextFill(Color.web(MUTED));
        perTopicLabel.setWrapText(true);

        VBox card = new VBox(6, title, stats, gap(2), perTopicTitle, perTopicLabel);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(cardStyle());
        VBox.setMargin(card, new Insets(0, 20, 8, 20));
        return card;
    }

    private VBox buildActivity() {
        Label logTitle = sectionLabel("ACTIVITY LOG");

        searchField = new TextField();
        searchField.setPromptText("🔍  Filter log…");
        searchField.setPrefHeight(30);
        searchField.setPrefWidth(190);
        styleField(searchField);
        searchField.textProperty().addListener((o, ov, nv) -> refreshLog());

        Button clearBtn = pill("Clear");
        clearBtn.setOnAction(e -> clearLog());

        Button exportBtn = pill("⤓  Export");
        exportBtn.setOnAction(e -> exportLog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(8, logTitle, spacer, searchField, clearBtn, exportBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(4);
        logArea.setStyle(
                "-fx-control-inner-background: " + SURFACE + ";" +
                "-fx-background-color: " + SURFACE + ";" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-font-family: " + MONO + ";" +
                "-fx-font-size: 11.5;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        VBox box = new VBox(6, toolbar, logArea);
        VBox.setVgrow(box, Priority.ALWAYS);
        VBox.setMargin(box, new Insets(0, 20, 8, 20));
        return box;
    }

    private HBox buildFooter() {
        autoClearBox = new CheckBox("Auto-clear message field");
        autoClearBox.setSelected(true);
        autoClearBox.setTextFill(Color.web(MUTED));
        autoClearBox.setFont(Font.font(UI, 11));

        timestampBox = new CheckBox("Show timestamps");
        timestampBox.setSelected(true);
        timestampBox.setTextFill(Color.web(MUTED));
        timestampBox.setFont(Font.font(UI, 11));
        timestampBox.selectedProperty().addListener((o, ov, nv) -> refreshLog());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label ver = new Label("v2.0 Pro");
        ver.setTextFill(Color.web(DIM));
        ver.setFont(Font.font(MONO, 10));

        HBox foot = new HBox(16, autoClearBox, timestampBox, sp, ver);
        foot.setPadding(new Insets(0, 22, 14, 22));
        foot.setAlignment(Pos.CENTER_LEFT);
        return foot;
    }

    private void publish() {
        String topic = resolveTopic();
        String msg = messageArea.getText().trim();
        if (topic.isEmpty() || msg.isEmpty()) {
            statusText.setText("⚠   Topic and message are required");
            statusText.setTextFill(Color.web(WARNING));
            return;
        }

        String priority = priorityBox.getValue();
        String payload = topic + " " + msg;
        if (!"Normal".equals(priority)) payload = topic + " [" + priority + "] " + msg;

        publisher.send(payload);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String entry = "[" + ts + "]  " + payload;
        logHistory.add(entry);

        topicCounts.merge(topic, 1, Integer::sum);
        totalCount++;
        updateStats();
        refreshLog();

        statusText.setText("✓   Published to " + topic + "   ·   " + ts);
        statusText.setTextFill(Color.web(SUCCESS));

        if (autoClearBox.isSelected()) {
            messageArea.clear();
            messageArea.requestFocus();
        }
        flashSuccess();
    }

    private void flashSuccess() {
        if (logArea == null) return;
        String cur = logArea.getStyle();
        logArea.setStyle(cur.replace("-fx-border-color: " + BORDER + ";",
                                     "-fx-border-color: " + SUCCESS + ";"));
        PauseTransition p = new PauseTransition(Duration.millis(400));
        p.setOnFinished(e -> logArea.setStyle(cur));
        p.play();
    }

    private void updateStats() {
        counterLabel.setText(String.valueOf(totalCount));
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> e : topicCounts.entrySet()) {
            if (e.getValue() == 0) continue;
            if (!first) sb.append("     ");
            first = false;
            sb.append(e.getKey()).append(" · ").append(e.getValue());
        }
        perTopicLabel.setText(sb.length() == 0 ? "No messages yet" : sb.toString());
    }

    private void refreshLog() {
        String filter = searchField.getText().trim().toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String line : logHistory) {
            if (filter.isEmpty() || line.toLowerCase().contains(filter)) {
                sb.append(timestampBox == null || timestampBox.isSelected() ? line : stripTime(line)).append("\n");
            }
        }
        logArea.setText(sb.toString());
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    private String stripTime(String line) {
        int i = line.indexOf(']');
        return i >= 0 ? line.substring(i + 2) : line;
    }

    private void clearLog() {
        logHistory.clear();
        topicCounts.replaceAll((k, v) -> 0);
        totalCount = 0;
        updateStats();
        refreshLog();
        statusText.setText("Activity cleared");
        statusText.setTextFill(Color.web(MUTED));
    }

    private void exportLog() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export activity log");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        fc.setInitialFileName("publisher-log.txt");
        File f = fc.showSaveDialog(null);
        if (f == null) return;
        try (PrintWriter pw = new PrintWriter(f)) {
            for (String line : logHistory) pw.println(line);
            statusText.setText("✓   Exported to " + f.getName());
            statusText.setTextFill(Color.web(SUCCESS));
        } catch (Exception ex) {
            statusText.setText("⚠   Export failed: " + ex.getMessage());
            statusText.setTextFill(Color.web(DANGER));
        }
    }

    private String resolveTopic() {
        String custom = customTopic.getText().trim();
        if (!custom.isEmpty()) return custom.toUpperCase();
        if (chipGroup.getSelectedToggle() != null)
            return ((ToggleButton) chipGroup.getSelectedToggle()).getText();
        return "";
    }

    private void shutdown() {
        try { publisher.close(); } catch (Exception ignored) {}
        try { context.term(); } catch (Exception ignored) {}
    }

    private ToggleButton makeChip(String topic) {
        String color = TOPIC_COLORS.getOrDefault(topic, ACCENT);
        ToggleButton b = new ToggleButton(topic);
        b.setToggleGroup(chipGroup);
        b.setFont(Font.font(UI, FontWeight.BOLD, 11));
        b.setPrefHeight(32);
        b.setPadding(new Insets(0, 16, 0, 16));
        b.setCursor(Cursor.HAND);
        b.setStyle(chipOff());
        b.selectedProperty().addListener((o, w, is) -> {
            b.setStyle(is ? chipOn(color) : chipOff());
            if (is && customTopic != null) customTopic.clear();
        });
        return b;
    }

    private String chipOff() {
        return "-fx-background-color: " + SURFACE2 + ";" +
               "-fx-text-fill: " + MUTED + ";" +
               "-fx-background-radius: 20;" +
               "-fx-border-color: " + BORDER + ";" +
               "-fx-border-radius: 20;" +
               "-fx-cursor: hand;";
    }

    private String chipOn(String c) {
        return "-fx-background-color: " + alpha(c, 0.15) + ";" +
               "-fx-text-fill: " + c + ";" +
               "-fx-background-radius: 20;" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-radius: 20;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, " + alpha(c, 0.4) + ", 12, 0, 0, 0);";
    }

    private Button pill(String text) {
        Button b = new Button(text);
        b.setFont(Font.font(UI, FontWeight.BOLD, 11));
        b.setTextFill(Color.web(MUTED));
        b.setPrefHeight(28);
        b.setPadding(new Insets(0, 12, 0, 12));
        b.setCursor(Cursor.HAND);
        b.setStyle(
                "-fx-background-color: " + SURFACE2 + ";" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 18;");
        b.setOnMouseEntered(e -> {
            b.setTextFill(Color.web(TEXT));
            b.setStyle(
                    "-fx-background-color: " + SURFACE3 + ";" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-color: " + ACCENT + ";" +
                    "-fx-border-radius: 18;");
        });
        b.setOnMouseExited(e -> {
            b.setTextFill(Color.web(MUTED));
            b.setStyle(
                    "-fx-background-color: " + SURFACE2 + ";" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 18;");
        });
        return b;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(UI, FontWeight.BOLD, 10.5));
        l.setTextFill(Color.web(DIM));
        return l;
    }

    private VBox vbox(double gap, javafx.scene.Node... nodes) {
        return new VBox(gap, nodes);
    }

    private Region gap(double h) { Region r = new Region(); r.setMinHeight(h); return r; }

    private Region buildDivider() {
        Region d = new Region();
        d.setPrefHeight(1);
        d.setStyle("-fx-background-color: " + BORDER + ";");
        return d;
    }

    private void pulse(Circle c) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.4), c);
        st.setFromX(1); st.setFromY(1);
        st.setToX(1.6); st.setToY(1.6);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.play();
    }

    private String cardStyle() {
        return "-fx-background-color: " + SURFACE + ";" +
               "-fx-background-radius: 16;" +
               "-fx-border-color: " + BORDER + ";" +
               "-fx-border-radius: 16;";
    }

    private String statCard(String color) {
        return "-fx-background-color: " + alpha(color, 0.10) + ";" +
               "-fx-background-radius: 12;" +
               "-fx-border-color: " + alpha(color, 0.35) + ";" +
               "-fx-border-radius: 12;";
    }

    private String gradient(String c1, String c2) {
        return "-fx-background-color: linear-gradient(to right, " + c1 + ", " + c2 + ");" +
               "-fx-background-radius: 12;" +
               "-fx-text-fill: white;" +
               "-fx-cursor: hand;";
    }

    private String alpha(String hex, double a) {
        Color c = Color.web(hex);
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255), a);
    }

    private void styleField(Control c) {
        c.setStyle(
                "-fx-control-inner-background: " + SURFACE2 + ";" +
                "-fx-background-color: " + SURFACE2 + ";" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-prompt-text-fill: " + DIM + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-font-family: '" + UI + "',sans-serif;" +
                "-fx-font-size: 12.5;");
    }

    private String inlineCss() {
        return "data:text/css," +
                ".combo-box .list-cell{ -fx-background-color:" + SURFACE2 + "; -fx-text-fill:" + TEXT + "; }" +
                ".combo-box-popup .list-view{ -fx-background-color:" + SURFACE2 + "; -fx-border-color:" + BORDER + "; }" +
                ".combo-box-popup .list-cell:hover{ -fx-background-color:" + SURFACE3 + "; }" +
                ".scroll-bar:vertical{ -fx-background-color:" + SURFACE + "; }" +
                ".scroll-bar .thumb{ -fx-background-color:" + BORDER + "; -fx-background-radius:6; }";
    }

    private void info(String title, String body) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(body);
        a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
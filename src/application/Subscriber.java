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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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

public class Subscriber extends Application {

    private static final String BG       = "#0a0c12";
    private static final String SURFACE  = "#12151f";
    private static final String SURFACE2 = "#1a1e2b";
    private static final String SURFACE3 = "#232838";
    private static final String BORDER   = "#2a3142";
    private static final String TEXT     = "#eaeef7";
    private static final String MUTED    = "#8b93a7";
    private static final String DIM      = "#565e72";
    private static final String ACCENT   = "#06b6d4";
    private static final String ACCENT2  = "#0ea5e9";
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

    private ZMQ.Context context;
    private ZMQ.Socket  subscriber;
    private Thread      receiverThread;

    private TextField        customTopic;
    private FlowPane         activeChips;
    private TextArea         logArea;
    private TextField        searchField;
    private Label            statusText;
    private Label            counterLabel;
    private Label            perTopicLabel;
    private Circle           statusDot;
    private ToggleGroup      chipGroup;
    private CheckBox         soundBox;
    private CheckBox         autoScrollBox;
    private CheckBox         timestampBox;
    private ComboBox<String> highlightBox;

    private final List<String>         logHistory    = new ArrayList<>();
    private final List<String>         subscriptions = new ArrayList<>();
    private final Map<String, Integer> topicCounts   = new LinkedHashMap<>();
    private int                        totalCount    = 0;

    @Override
    public void start(Stage stage) {
        context    = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        subscriber.connect("tcp://localhost:5555");

        for (String t : TOPIC_COLORS.keySet()) topicCounts.put(t, 0);

        double screenW = Screen.getPrimary().getVisualBounds().getWidth();
        double screenH = Screen.getPrimary().getVisualBounds().getHeight();
        double w = Math.min(700, screenW - 80);
        double h = Math.min(800, screenH - 80);

        Scene scene = new Scene(buildUI(), w, h);
        scene.getStylesheets().add(inlineCss());

        stage.setTitle("ZeroMQ  ·  Subscriber Pro");
        stage.setMinWidth(520);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.centerOnScreen();
        stage.show();

        receiverThread = new Thread(this::receiveLoop, "sub-receiver");
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private Parent buildUI() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + BG + ";");
        root.getChildren().addAll(
                buildHeader(),
                buildDivider(),
                buildSubscribeCard(),
                buildStats(),
                buildInbox(),
                buildFooter());
        VBox.setVgrow(root.getChildren().get(4), Priority.ALWAYS);
        return root;
    }

    private HBox buildHeader() {
        statusDot = new Circle(5, Color.web(SUCCESS));
        pulse(statusDot);

        Label brand = new Label("Subscriber Pro");
        brand.setFont(Font.font(UI, FontWeight.BOLD, 22));
        brand.setTextFill(Color.web(TEXT));

        HBox title = new HBox(8, statusDot, brand);
        title.setAlignment(Pos.CENTER_LEFT);

        statusText = new Label("Connected to tcp://localhost:5555");
        statusText.setFont(Font.font(UI, 11.5));
        statusText.setTextFill(Color.web(MUTED));

        VBox left = new VBox(4, title, statusText);
        left.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button infoBtn = pill("?  Info");
        infoBtn.setOnAction(e -> info("Subscriber",
                "Subscribe to topics. Messages matching your subscriptions appear in the inbox.\n\n" +
                "Try subscribing to COURSE, then publish a COURSE message from the Publisher."));

        HBox header = new HBox(left, spacer, infoBtn);
        header.setPadding(new Insets(18, 22, 14, 22));
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildSubscribeCard() {
        Label topicLbl = sectionLabel("PICK TOPIC");

        customTopic = new TextField();
        customTopic.setPromptText("Or type a custom topic…");
        customTopic.setPrefHeight(36);
        styleField(customTopic);
        customTopic.setOnAction(e -> subscribe());

        chipGroup = new ToggleGroup();
        FlowPane chips = new FlowPane(8, 8);
        for (String t : TOPIC_COLORS.keySet()) chips.getChildren().add(makeChip(t));
        ((ToggleButton) chips.getChildren().get(0)).setSelected(true);

        Button subBtn = new Button("Subscribe");
        subBtn.setPrefHeight(36);
        subBtn.setPrefWidth(120);
        subBtn.setFont(Font.font(UI, FontWeight.BOLD, 12.5));
        subBtn.setTextFill(Color.WHITE);
        subBtn.setCursor(Cursor.HAND);
        subBtn.setStyle(gradient(ACCENT2, ACCENT));
        subBtn.setOnMouseEntered(e -> subBtn.setStyle(
                gradient("#22b8dc", "#38bdf8") +
                "-fx-effect: dropshadow(gaussian, " + alpha(ACCENT, 0.55) + ", 18, 0, 0, 4);"));
        subBtn.setOnMouseExited(e -> subBtn.setStyle(gradient(ACCENT2, ACCENT)));
        subBtn.setOnAction(e -> subscribe());

        HBox actionRow = new HBox(10, customTopic, subBtn);
        HBox.setHgrow(customTopic, Priority.ALWAYS);

        Label activeLbl = sectionLabel("ACTIVE SUBSCRIPTIONS");
        activeChips = new FlowPane(8, 8);
        activeChips.setPadding(new Insets(4, 0, 0, 0));
        activeChips.setMinHeight(32);

        VBox card = new VBox(6,
                topicLbl, chips,
                gap(4), actionRow,
                gap(6), activeLbl, activeChips);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());
        VBox.setMargin(card, new Insets(12, 20, 8, 20));
        return card;
    }

    private VBox buildStats() {
        Label title = sectionLabel("STATISTICS");

        counterLabel = new Label("0");
        counterLabel.setFont(Font.font(UI, FontWeight.BOLD, 20));
        counterLabel.setTextFill(Color.web(ACCENT));

        Label totalLbl = new Label("RECEIVED");
        totalLbl.setFont(Font.font(UI, FontWeight.BOLD, 10));
        totalLbl.setTextFill(Color.web(DIM));

        VBox total = new VBox(2, counterLabel, totalLbl);
        total.setAlignment(Pos.CENTER);
        total.setPadding(new Insets(8, 12, 8, 12));
        total.setStyle(statCard(ACCENT));

        Label perLbl = sectionLabel("PER TOPIC");
        perTopicLabel = new Label("Nothing yet");
        perTopicLabel.setFont(Font.font(MONO, 11));
        perTopicLabel.setTextFill(Color.web(MUTED));
        perTopicLabel.setWrapText(true);

        HBox stats = new HBox(8, total);
        VBox card = new VBox(6, title, stats, gap(2), perLbl, perTopicLabel);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(cardStyle());
        VBox.setMargin(card, new Insets(0, 20, 8, 20));
        return card;
    }

    private VBox buildInbox() {
        Label logTitle = sectionLabel("INBOX");

        searchField = new TextField();
        searchField.setPromptText("🔍  Filter…");
        searchField.setPrefHeight(30);
        searchField.setPrefWidth(150);
        styleField(searchField);
        searchField.textProperty().addListener((o, ov, nv) -> refreshLog());

        highlightBox = new ComboBox<>();
        highlightBox.getItems().addAll("All topics", "COURSE", "EXAM", "EVENT", "NEWS");
        highlightBox.setValue("All topics");
        highlightBox.setPrefHeight(30);
        highlightBox.setPrefWidth(115);
        styleField(highlightBox);
        highlightBox.valueProperty().addListener((o, ov, nv) -> refreshLog());

        Button clearBtn = pill("Clear");
        clearBtn.setOnAction(e -> clearInbox());

        Button exportBtn = pill("⤓  Export");
        exportBtn.setOnAction(e -> exportInbox());

        Button copyBtn = pill("⎘  Copy");
        copyBtn.setOnAction(e -> copyInbox());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox toolbar = new HBox(6, logTitle, sp, highlightBox, searchField, copyBtn, clearBtn, exportBtn);
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
        soundBox = new CheckBox("Sound alert");
        soundBox.setSelected(false);
        soundBox.setTextFill(Color.web(MUTED));
        soundBox.setFont(Font.font(UI, 11));

        autoScrollBox = new CheckBox("Auto-scroll");
        autoScrollBox.setSelected(true);
        autoScrollBox.setTextFill(Color.web(MUTED));
        autoScrollBox.setFont(Font.font(UI, 11));

        timestampBox = new CheckBox("Timestamps");
        timestampBox.setSelected(true);
        timestampBox.setTextFill(Color.web(MUTED));
        timestampBox.setFont(Font.font(UI, 11));
        timestampBox.selectedProperty().addListener((o, ov, nv) -> refreshLog());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label ver = new Label("v2.0 Pro");
        ver.setTextFill(Color.web(DIM));
        ver.setFont(Font.font(MONO, 10));

        HBox foot = new HBox(16, soundBox, autoScrollBox, timestampBox, sp, ver);
        foot.setPadding(new Insets(0, 22, 14, 22));
        foot.setAlignment(Pos.CENTER_LEFT);
        return foot;
    }

    private void subscribe() {
        String topic = resolveTopic();
        if (topic.isEmpty()) return;
        if (subscriptions.contains(topic)) {
            statusText.setText("Already subscribed to \"" + topic + "\"");
            statusText.setTextFill(Color.web(WARNING));
            return;
        }
        subscriber.subscribe(topic.getBytes());
        subscriptions.add(topic);
        if (!topicCounts.containsKey(topic)) topicCounts.put(topic, 0);
        addActiveChip(topic);
        statusText.setText("✓  Subscribed to \"" + topic + "\"");
        statusText.setTextFill(Color.web(SUCCESS));
        customTopic.clear();
    }

    private String resolveTopic() {
        String custom = customTopic.getText().trim();
        if (!custom.isEmpty()) return custom.toUpperCase();
        if (chipGroup.getSelectedToggle() != null)
            return ((ToggleButton) chipGroup.getSelectedToggle()).getText();
        return "";
    }

    private void addActiveChip(String topic) {
        String color = TOPIC_COLORS.getOrDefault(topic, ACCENT);
        Label chip = new Label("●  " + topic + "   ✕");
        chip.setFont(Font.font(UI, FontWeight.BOLD, 10.5));
        chip.setTextFill(Color.web(color));
        chip.setPadding(new Insets(4, 12, 4, 12));
        chip.setCursor(Cursor.HAND);
        chip.setStyle(
                "-fx-background-color: " + alpha(color, 0.14) + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + alpha(color, 0.45) + ";" +
                "-fx-border-radius: 14;");
        chip.setOnMouseClicked(e -> unsubscribe(topic, chip));
        activeChips.getChildren().add(chip);
    }

    private void unsubscribe(String topic, Label chip) {
        try { subscriber.unsubscribe(topic.getBytes()); } catch (Exception ignored) {}
        subscriptions.remove(topic);
        activeChips.getChildren().remove(chip);
        statusText.setText("Unsubscribed from \"" + topic + "\"");
        statusText.setTextFill(Color.web(MUTED));
    }

    private void receiveLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String msg = subscriber.recvStr();
                if (msg == null) continue;
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                Platform.runLater(() -> onMessage(msg, ts));
            } catch (Exception ex) {
                if (Thread.currentThread().isInterrupted()) break;
            }
        }
    }

    private void onMessage(String msg, String ts) {
        logHistory.add("[" + ts + "]  " + msg);
        totalCount++;

        String topic = msg.split(" ", 2)[0];
        topicCounts.merge(topic, 1, Integer::sum);
        updateStats();
        refreshLog();

        statusText.setText("●  New message received   ·   " + ts);
        statusText.setTextFill(Color.web(SUCCESS));
        flashInbox();

        if (soundBox != null && soundBox.isSelected()) playPing();
    }

    private void flashInbox() {
        if (logArea == null) return;
        String cur = logArea.getStyle();
        logArea.setStyle(cur.replace("-fx-border-color: " + BORDER + ";",
                                     "-fx-border-color: " + SUCCESS + ";"));
        PauseTransition p = new PauseTransition(Duration.millis(500));
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
        perTopicLabel.setText(sb.length() == 0 ? "Nothing yet" : sb.toString());
    }

    private void refreshLog() {
        String filter = searchField.getText().trim().toLowerCase();
        String hl = highlightBox.getValue();
        StringBuilder sb = new StringBuilder();
        for (String line : logHistory) {
            if (filter.isEmpty() || line.toLowerCase().contains(filter)) {
                if (!"All topics".equals(hl) && !line.toLowerCase().contains(hl.toLowerCase())) continue;
                sb.append(timestampBox == null || timestampBox.isSelected() ? line : stripTime(line)).append("\n");
            }
        }
        logArea.setText(sb.toString());
        if (autoScrollBox == null || autoScrollBox.isSelected()) logArea.setScrollTop(Double.MAX_VALUE);
    }

    private String stripTime(String line) {
        int i = line.indexOf(']');
        return i >= 0 ? line.substring(i + 2) : line;
    }

    private void clearInbox() {
        logHistory.clear();
        topicCounts.replaceAll((k, v) -> 0);
        totalCount = 0;
        updateStats();
        refreshLog();
        statusText.setText("Inbox cleared");
        statusText.setTextFill(Color.web(MUTED));
    }

    private void exportInbox() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export inbox");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        fc.setInitialFileName("inbox.txt");
        File f = fc.showSaveDialog(null);
        if (f == null) return;
        try (PrintWriter pw = new PrintWriter(f)) {
            for (String l : logHistory) pw.println(l);
            statusText.setText("✓  Exported to " + f.getName());
            statusText.setTextFill(Color.web(SUCCESS));
        } catch (Exception ex) {
            statusText.setText("⚠  Export failed: " + ex.getMessage());
            statusText.setTextFill(Color.web(DANGER));
        }
    }

    private void copyInbox() {
        ClipboardContent cc = new ClipboardContent();
        cc.putString(logArea.getText());
        Clipboard.getSystemClipboard().setContent(cc);
        statusText.setText("✓  Copied inbox to clipboard");
        statusText.setTextFill(Color.web(SUCCESS));
    }

    private void playPing() {
        try {
            Class<?> tk = Class.forName("java.awt.Toolkit");
            Object def = tk.getMethod("getDefaultToolkit").invoke(null);
            tk.getMethod("beep").invoke(def);
        } catch (Throwable ignored) {}
    }

    private void shutdown() {
        if (receiverThread != null) receiverThread.interrupt();
        try { subscriber.close(); } catch (Exception ignored) {}
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
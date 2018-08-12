package main.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.model.ImageSet;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    final static int THIRTY_SECONDS = 30000;
    final static int ONE_MINUTE = 60000;
    final static int TWO_MINUTES = ONE_MINUTE * 2;
    final static int THREE_MINUTES = ONE_MINUTE * 3;
    final static int FIVE_MINUTES = ONE_MINUTE * 5;
    final static int TEN_MINUTES = FIVE_MINUTES * 2;
    final static int FIFTEEN_MINUTES = TEN_MINUTES + FIVE_MINUTES;
    final static int THIRTY_MINUTES = FIFTEEN_MINUTES * 2;

    Path path;
    ArrayList<ImageSet> imageArray;

    Button startButton = new Button("Start");
    Button stopButton = new Button("Stop!");

    ScrollPane scrollPane;

    Label timeLabel = new Label("Time per Image");
    ComboBox timeComboBox = new ComboBox();

    ObservableList<String> timeCBOptions = FXCollections.observableArrayList("30 Seconds", "1 minute", "2 minutes",
            "3 minutes", "5 minutes", "10 minutes", "15 minutes", "30 minutes");

    Timeline timeline = new Timeline();
    private Timer timer;
    private Instant startClock;

    private int nPrev = -1;
    private int nActual = 0;

    @FXML
    ImageView image;

    @FXML
    Button wdButton;

    @FXML
    Button setButton;

    @FXML
    ButtonBar buttonBar;

    @FXML
    TextFlow textFlow;

    @FXML
    BorderPane pane;

    @FXML
    Button nxtButton;

    @FXML
    Button prvButton;

    @FXML
    VBox vbox;

    @FXML
    Text messageText;
    private Thread threadTimer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane = generateInnerPane();

        buttonBar.setButtonData(wdButton, ButtonBar.ButtonData.LEFT);
        buttonBar.setButtonData(setButton, ButtonBar.ButtonData.LEFT);

        wdButton.setOnAction(event -> {
            if (openDirectory()) {
                imageArray = retrieveSet();
                messageText.setText("Directory Established, " + imageArray.size() + " images loaded.");
            }
        });
        wdButton.setId("wdButton");

        setButton.setOnAction(e -> popupSettingsPane());
        setButton.setId("setButton");

        startButton.setOnAction(e -> {
            if (startTimer()) {
                nActual = new Random().nextInt(imageArray.size());
                setImage(imageArray.get(
                        nActual).getUrl());
            }
        });
        startButton.setPrefWidth(120);
        startButton.setId("startButton");

        stopButton.setPrefWidth(120);
        stopButton.setId("stopButton");
        stopButton.setOnAction(event -> stopTimer());

        prvButton.setOnAction(e -> {
            timer.stop();
            setBackImage();
            timer.start();
            startClock = Instant.now();
        });

        nxtButton.setOnAction(e -> {
            timer.stop();
            nPrev = nActual;
            nActual = new Random().nextInt(imageArray.size());
            setImage(imageArray.get(
                    nActual).getUrl());
            timer.start();
            startClock = Instant.now();
        });

        image.fitWidthProperty().bind(pane.widthProperty());
        image.fitHeightProperty().bind(pane.heightProperty().subtract(vbox.heightProperty())
                .subtract(textFlow.heightProperty()));

        setTimer();
    }

    private ScrollPane generateInnerPane() {
        VBox vbox = new VBox(50);
        VBox innerVBox = new VBox(10);
        innerVBox.setPadding(new Insets(5));
        timeComboBox.getItems().addAll(timeCBOptions);
        innerVBox.getChildren().addAll(timeLabel, timeComboBox, startButton, stopButton);
        innerVBox.alignmentProperty().setValue(Pos.TOP_CENTER);
        vbox.getChildren().add(innerVBox);
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefWidth(150);
        scrollPane.setMaxWidth(400);
        return scrollPane;
    }

    private boolean openDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Directory");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        try {
            path = directoryChooser.showDialog(new Stage()).toPath();
            return true;
        } catch (Exception e) {
            messageText.setText("No Valid Directory Selected");
            return false;
        }
    }

    private ArrayList<ImageSet> retrieveSet() {
        ArrayList<ImageSet> imageSetArrayList = new ArrayList<>();
        try {
            Files.list(path).filter(p -> p.getFileName().toString().endsWith(".jpg") ||
                    p.getFileName().toString().endsWith(".jpeg") ||
                    p.getFileName().toString().endsWith(".png"))
                    .forEach(p -> imageSetArrayList.add(
                            new ImageSet(p.toAbsolutePath().toString(), p.getFileName().toString())));
        } catch (IOException e) {
            messageText.setText("Error while retrieving images!");
        }
        return imageSetArrayList;
    }

    private void popupSettingsPane() {
        if (pane.getLeft() != null) {
            fadeOutNode(pane.getLeft());
            timeline.setOnFinished(e -> pane.setLeft(null));
        } else {
            timeline.setOnFinished(null);
            pane.setLeft(scrollPane);
            fadeInNode(pane.getLeft());
            pane.getLeft().setVisible(true);
        }

    }

    private boolean startTimer() {
        timer = null;
        try {
            timer = new Timer(parseTime(), e -> {
                randomImage();
                startClock = Instant.now();
            }
            );
            timer.start();
            startClock = Instant.now();
            timerOnScreen();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setImage(String url) {
        Image imageElement = null;
        try {
            imageElement = new Image("file:" + url);
            image.setImage(imageElement);
        } catch (Exception e) {
            messageText.setText("Error: File not found!");
        }
    }

    private void stopTimer() {
        try {
            timer.stop();
            threadTimer.interrupt();
        } catch (Exception e) {
        }
    }

    private void setBackImage() {
        if (nPrev == -1)
            return;
        nActual = nPrev;
        setImage(imageArray.get(nPrev).getUrl());
    }


    private void timerOnScreen() {
        System.out.println("Thread isAlive: " + threadTimer.isAlive());
        if(threadTimer.isAlive())
            threadTimer.start();
        else {
            threadTimer.start();
        }
    }

    private void setTimer(){
        threadTimer = new Thread(() -> {
            while (timer.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                try {
                    java.time.Duration endClock = java.time.Duration.ofMillis(parseTime() + 1000);
                    java.time.Duration actualCounter = java.time.Duration.between(startClock, Instant.now());
                    java.time.Duration remainingTime = endClock.minus(actualCounter);
                    final String timeS = remainingTime.toMinutes() + " minutes " +
                            remainingTime.getSeconds() + " seconds";
                    Platform.runLater(() -> {
                        messageText.setText(timeS);
                    });
                } catch (Exception e) {}

            }
        });
    }

    private int parseTime() throws Exception {
        switch (timeComboBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                return THIRTY_SECONDS;
            case 1:
                return ONE_MINUTE;
            case 2:
                return TWO_MINUTES;
            case 3:
                return THREE_MINUTES;
            case 4:
                return FIVE_MINUTES;
            case 5:
                return TEN_MINUTES;
            case 6:
                return FIFTEEN_MINUTES;
            case 7:
                return THIRTY_MINUTES;
            default:
                messageText.setText("Error: No time option selected!");
                throw new Exception();
        }
    }

    private void randomImage() {
        nPrev = nActual;
        nActual = new Random().nextInt(imageArray.size());
        setImage(imageArray.get(nActual).getUrl());
    }

    private void fadeInNode(Node node) {
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.opacityProperty(), 0.0)),
                new KeyFrame(new Duration(1200),
                        new KeyValue(node.opacityProperty(), 1.0)));
        timeline.play();
    }

    private void fadeOutNode(Node node) {
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.opacityProperty(), 1.0)),
                new KeyFrame(new Duration(1200),
                        new KeyValue(node.opacityProperty(), 0.0)));
        timeline.play();
    }

}

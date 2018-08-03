package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage publicWindow;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../resources/fxmls/mainScene.fxml"));
        primaryStage.setTitle("RefMe");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("../resources/styles/mainScene.css")
        .toExternalForm());
        publicWindow = primaryStage;
        publicWindow.getIcons().add(new Image(getClass().getResourceAsStream("../resources/images/RefMe.PNG")));
        publicWindow.setScene(scene);
        publicWindow.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

package org.registry.akashic.akashicjavafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.registry.akashic.akashicjavafx.controller.HomeController;

public class AkashicApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homepage.fxml"));
        Parent root = loader.load();
        HomeController controller = loader.getController();

        primaryStage.setTitle("Akashic");
        primaryStage.setUserData(controller);
        primaryStage.setScene(new Scene(root, 1220, 740));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
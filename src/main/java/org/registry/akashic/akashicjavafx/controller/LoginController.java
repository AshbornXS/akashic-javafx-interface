package org.registry.akashic.akashicjavafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private AkashicController mainController;

    public void setMainController(AkashicController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    System.out.println(response);
                    String token = parseToken(response);
                    saveToken(token);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Login");
                        alert.setHeaderText(null);
                        alert.setContentText("Login successful!");
                        alert.showAndWait();
                        notifyMainController();
                    });
                })
                .exceptionally(_ -> {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Login");
                        alert.setHeaderText(null);
                        alert.setContentText("Login failed!");
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    private String parseToken(String response) {
        // Split the response by comma and return the first part as the token
        String[] parts = response.split(",");
        return parts[0].trim();
    }

    private void saveToken(String token) {
        try {
            Files.writeString(Paths.get("token.txt"), token, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyMainController() {
        if (mainController != null) {
            mainController.setLoggedIn(true);
        } else {
            System.err.println("Main controller is null");
        }
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    public void goToRegister() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        mainController.openWindow("/fxml/register.fxml", "Cadastro", mainController);
        stage.close();
    }
}
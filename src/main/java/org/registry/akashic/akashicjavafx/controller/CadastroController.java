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

public class CadastroController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField nameField;
    @FXML
    private PasswordField passwordField;

    private AkashicController mainController;

    public void setMainController(AkashicController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String name = nameField.getText();
        String password = passwordField.getText();

        String json = String.format("{\"username\":\"%s\", \"name\":\"%s\", \"password\":\"%s\", \"role\":\"ROLE_USER\"}", email, name, password);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(_ -> Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Registration");
                    alert.setHeaderText(null);
                    alert.setContentText("Registration successful!");
                    alert.showAndWait();
                    notifyMainController();
                }))
                .exceptionally(_ -> {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Registration");
                        alert.setHeaderText(null);
                        alert.setContentText("Registration failed!");
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    private void notifyMainController() {
        if (mainController != null) {
            mainController.setLoggedIn(true);
        } else {
            System.err.println("Main controller is null");
        }
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    public void goToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        mainController.openWindow("/fxml/login.fxml", "Login", mainController);
        stage.close();
    }
}
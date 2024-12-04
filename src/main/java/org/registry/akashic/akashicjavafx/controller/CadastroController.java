// CadastroController.java
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

public class CadastroController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField nameField;
    @FXML
    private PasswordField passwordField;

    private HomeController mainController;

    public void setMainController(HomeController mainController) {
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
                    performLogin(email, password);
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

    private void performLogin(String email, String password) {
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", email, password);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    String token = parseToken(response);
                    String role = parseRole(response);
                    saveTokenAndRole(token, role);
                    Platform.runLater(this::notifyMainController);
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
        String[] parts = response.split(",");
        return parts[0].trim();
    }

    private String parseRole(String response) {
        String[] parts = response.split(",");
        return parts[1].trim();
    }

    private void saveTokenAndRole(String token, String role) {
        try {
            String info = token + "," + role;
            Files.writeString(Paths.get("info.txt"), info, StandardCharsets.UTF_8);
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
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    public void goToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        mainController.openWindow("/fxml/login.fxml", "Login", mainController);
        stage.close();
    }
}
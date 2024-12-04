package org.registry.akashic.akashicjavafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HomeController {
    @FXML
    private Pane menuPane;
    @FXML
    private Button adminButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button profileButton;

    private boolean isLoggedIn = false;

    @FXML
    private void initialize() {
        checkToken();
        updateButtonVisibility();
    }

    private void checkToken() {
        try {
            String info = Files.readString(Paths.get("info.txt"));
            String token = info.split(",")[0];
            if (!token.isEmpty()) {
                setLoggedIn(true);
            }
        } catch (IOException e) {
            setLoggedIn(false);
        }
    }

    private String checkUserRole() {
        try {
            String info = Files.readString(Paths.get("info.txt"));
            return info.split(",")[1];
        } catch (IOException e) {
            return "";
        }
    }

    @FXML
    private void handleBooks() {
        openWindow("/fxml/book_list.fxml", "Livros", this);
    }

    @FXML
    private void handleAdmin() {
        openWindow("/fxml/admin.fxml", "Admin", this);
    }

    @FXML
    private void handleLogin() {
        openWindow("/fxml/login.fxml", "Login", this);
    }

    @FXML
    private void handleRegister() {
        openWindow("/fxml/register.fxml", "Cadastro", this);
    }

    @FXML
    private void handleProfile() {
        try {
            String info = Files.readString(Paths.get("info.txt"));
            String token = info.split(",")[0];
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8081/auth/me")).header("Authorization", "Bearer " + token).GET().build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(response -> {
                Platform.runLater(() -> openProfileWindow(response));
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Files.deleteIfExists(Paths.get("info.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setLoggedIn(false);
    }

    public void setLoggedIn(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (isLoggedIn) {
            if (checkUserRole().equals("ROLE_ADMIN")) {
                if (!menuPane.getChildren().contains(adminButton)) {
                    menuPane.getChildren().add(adminButton);
                }
            } else {
                menuPane.getChildren().remove(adminButton);
            }

            menuPane.getChildren().remove(loginButton);
            menuPane.getChildren().remove(registerButton);
            if (!menuPane.getChildren().contains(logoutButton)) {
                menuPane.getChildren().add(logoutButton);
            }
            if (!menuPane.getChildren().contains(profileButton)) {
                menuPane.getChildren().add(profileButton);
            }
        } else {
            menuPane.getChildren().remove(adminButton);
            if (!menuPane.getChildren().contains(loginButton)) {
                menuPane.getChildren().add(loginButton);
            }
            if (!menuPane.getChildren().contains(registerButton)) {
                menuPane.getChildren().add(registerButton);
            }
            menuPane.getChildren().remove(logoutButton);
            menuPane.getChildren().remove(profileButton);
        }
    }

    private void openProfileWindow(String userInfo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.setUserInfo(userInfo);

            Stage stage = new Stage();
            stage.setTitle("Perfil");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void openWindow(String fxmlPath, String title, HomeController mainController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            // Passar a referência do AkashicController para o LoginController
            if (fxmlPath.equals("/fxml/login.fxml")) {
                LoginController loginController = loader.getController();
                loginController.setMainController(mainController);
            } else if (fxmlPath.equals("/fxml/register.fxml")) {
                CadastroController cadastroController = loader.getController();
                cadastroController.setMainController(mainController);
            } else if (fxmlPath.equals("/fxml/admin.fxml")) {
                AdminController adminController = loader.getController();
                adminController.setMainController(mainController);
            }

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

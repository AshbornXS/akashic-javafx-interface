// AkashicController.java
package org.registry.akashic.akashicjavafx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.registry.akashic.akashicjavafx.response.Book;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class AkashicController {
    @FXML
    private ScrollPane bookScrollPane;
    @FXML
    private FlowPane bookFlowPane;
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
        bookScrollPane.setFitToWidth(true);
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

    public void setBooks(List<Book> books) {
        for (Book book : books) {
            VBox vBox = new VBox(10); // 10 is the spacing between elements in VBox
            vBox.setAlignment(Pos.CENTER);
            vBox.setPrefWidth(150); // Set a fixed width for each VBox

            ImageView imageView = new ImageView();
            Text title = new Text();
            Text author = new Text(book.getAuthor());

            // Set the title with ellipsis if it exceeds a certain length
            String fullTitle = book.getTitle();
            if (fullTitle.length() > 20) {
                title.setText(fullTitle.substring(0, 17) + "...");
            } else {
                title.setText(fullTitle);
            }

            // Add a tooltip to show the full title on hover
            Tooltip tooltip = new Tooltip(fullTitle);
            Tooltip.install(title, tooltip);

            if (book.getImageData() != null && !book.getImageData().isEmpty()) {
                byte[] imageBytes = Base64.getDecoder().decode(book.getImageData());
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                imageView.setImage(image);
                imageView.setFitHeight(150); // Increase the height
                imageView.setFitWidth(150); // Increase the width
                imageView.setPreserveRatio(true); // Preserve the aspect ratio
            }

            vBox.getChildren().addAll(imageView, title, author);
            vBox.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    openBookDetails(book.getId());
                }
            });
            bookFlowPane.getChildren().add(vBox);
        }
    }

    private void openBookDetails(int bookId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book_details.fxml"));
            Parent root = loader.load();
            BookDetailsController controller = loader.getController();
            controller.loadBookDetails(bookId);

            Stage stage = new Stage();
            stage.setTitle("Book Details");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMinWidth(600); // Set minimum width
            stage.setMinHeight(800); // Set minimum height
            stage.setWidth(600); // Set preferred width
            stage.setHeight(800); // Set preferred height
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            bookFlowPane.getChildren().remove(loginButton);
            bookFlowPane.getChildren().remove(registerButton);
            if (!bookFlowPane.getChildren().contains(logoutButton)) {
                bookFlowPane.getChildren().add(logoutButton);
            }
            if (!bookFlowPane.getChildren().contains(profileButton)) {
                bookFlowPane.getChildren().add(profileButton);
            }
        } else {
            if (!bookFlowPane.getChildren().contains(loginButton)) {
                bookFlowPane.getChildren().add(loginButton);
            }
            if (!bookFlowPane.getChildren().contains(registerButton)) {
                bookFlowPane.getChildren().add(registerButton);
            }
            bookFlowPane.getChildren().remove(logoutButton);
            bookFlowPane.getChildren().remove(profileButton);
        }
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

    void openWindow(String fxmlPath, String title, AkashicController mainController) {
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
            }

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
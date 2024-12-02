// AkashicController.java
package org.registry.akashic.akashicjavafx;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    private boolean isLoggedIn = false;

    @FXML
    private void initialize() {
        checkToken();
        updateButtonVisibility();
        bookScrollPane.setFitToWidth(true);
    }


    private void checkToken() {
        try {
            String token = Files.readString(Paths.get("token.txt"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/book_details.fxml"));
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
        openWindow("/Login.fxml", "Login", this);
    }

    @FXML
    private void handleRegister() {
        openWindow("/Cadastro.fxml", "Cadastro", this);
    }

    @FXML
    private void handleLogout() {
        try {
            Files.deleteIfExists(Paths.get("token.txt"));
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
        } else {
            if (!bookFlowPane.getChildren().contains(loginButton)) {
                bookFlowPane.getChildren().add(loginButton);
            }
            if (!bookFlowPane.getChildren().contains(registerButton)) {
                bookFlowPane.getChildren().add(registerButton);
            }
            bookFlowPane.getChildren().remove(logoutButton);
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
            if (fxmlPath.equals("/Login.fxml")) {
                LoginController loginController = loader.getController();
                loginController.setMainController(mainController);
            } else if (fxmlPath.equals("/Cadastro.fxml")) {
                CadastroController cadastroController = loader.getController();
                cadastroController.setMainController(mainController);
            }

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
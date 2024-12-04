package org.registry.akashic.akashicjavafx.controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.registry.akashic.akashicjavafx.response.APIResponseBody;
import org.registry.akashic.akashicjavafx.response.Book;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class AkashicController {
    @FXML
    private ScrollPane bookScrollPane;
    @FXML
    private FlowPane bookFlowPane;

    @FXML
    private void initialize() {
        consumeApi();
        bookScrollPane.setFitToWidth(true);
    }

    private void consumeApi() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8081/books")).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(this::parseBooks).thenAccept(books -> {
            Platform.runLater(() -> {
                if (books != null) {
                    setBooks(books);
                } else {
                    System.out.println("No books received");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> System.out.println("Error fetching books: " + e.getMessage()));
            return null;
        });
    }

    private List<Book> parseBooks(String responseBody) {
        Gson gson = new Gson();
        Book[] booksArray = gson.fromJson(responseBody, Book[].class);
        return Arrays.asList(booksArray);
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
            if (fullTitle != null) {
                if (fullTitle.length() > 20) {
                    title.setText(fullTitle.substring(0, 17) + "...");
                } else {
                    title.setText(fullTitle);
                }

                // Add a tooltip to show the full title on hover
                Tooltip tooltip = new Tooltip(fullTitle);
                Tooltip.install(title, tooltip);
            } else {
                title.setText("No Title");
            }

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
            stage.setWidth(1220); // Set preferred width
            stage.setHeight(740); // Set preferred height
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
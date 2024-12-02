package org.registry.akashic.akashicjavafx;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class BookDetailsController {
    @FXML
    private ImageView bookImage;
    @FXML
    private Label bookTitle;
    @FXML
    private Label bookAuthor;
    @FXML
    private Label bookDescription;
    @FXML
    private Label bookTags;

    public void setBookDetails(Book book) {
        Platform.runLater(() -> {
            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());
            bookDescription.setText(book.getDescription());
            bookTags.setText(book.getTags());

            if (book.getImageData() != null && !book.getImageData().isEmpty()) {
                byte[] imageBytes = Base64.getDecoder().decode(book.getImageData());
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                bookImage.setImage(image);
            }
        });
    }

    public void loadBookDetails(int bookId) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/books/search?id=" + bookId))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseBook)
                .thenAccept(this::setBookDetails)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private Book parseBook(String responseBody) {
        Gson gson = new Gson();
        return gson.fromJson(responseBody, Book.class);
    }
}
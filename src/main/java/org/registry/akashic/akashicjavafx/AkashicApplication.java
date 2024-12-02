package org.registry.akashic.akashicjavafx;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.registry.akashic.akashicjavafx.controller.AkashicController;
import org.registry.akashic.akashicjavafx.response.ApiResponse;
import org.registry.akashic.akashicjavafx.response.Book;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AkashicApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book_list.fxml"));
        Parent root = loader.load();
        AkashicController controller = loader.getController();

        // Consumir a API
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8081/books")).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(this::parseBooks).thenAccept(books -> {
            Platform.runLater(() -> {
                if (books != null) {
                    controller.setBooks(books);
                } else {
                    System.out.println("No books received");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> System.out.println("Error fetching books: " + e.getMessage()));
            return null;
        });

        primaryStage.setTitle("Book List");
        primaryStage.setUserData(controller);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    private List<Book> parseBooks(String responseBody) {
        Gson gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(responseBody, ApiResponse.class);
        return apiResponse.getContent();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
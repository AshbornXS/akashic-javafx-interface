package org.registry.akashic.akashicjavafx.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.registry.akashic.akashicjavafx.domain.Book;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class AdminController {
    @FXML
    public TextField addTitle;
    @FXML
    public TextField addAuthor;
    @FXML
    public TextArea addDesc;

    @FXML
    public TextField editID;
    @FXML
    public TextField editTitle;
    @FXML
    public TextField editAuthor;
    @FXML
    public TextArea editDesc;

    @FXML
    public TextField deleteTitleText;
    @FXML
    public TextField deleteAuthorText;
    @FXML
    public TextArea deleteDescText;
    @FXML
    public TextField deleteID;


    @FXML
    private Button imageChooserButton;
    @FXML
    private Label imageNameLabel;

    private HomeController mainController;
    private File selectedImageFile; // Add this field

    public void setMainController(HomeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleAdd() {
        mainController.openWindow("/fxml/admin_add.fxml", "Adicionar", mainController);
    }

    @FXML
    private void handleDelete() {
        mainController.openWindow("/fxml/admin_delete.fxml", "Deletar", mainController);
    }

    @FXML
    private void handleUpdate() {
        mainController.openWindow("/fxml/admin_update.fxml", "Alterar", mainController);
    }

    @FXML
    private void openImageChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(imageChooserButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedImageFile = selectedFile; // Store the selected file
            imageNameLabel.setText(selectedFile.getName());
        } else {
            imageNameLabel.setText("Nenhuma imagem selecionada");
        }
    }

    @FXML
    private void addBook() {
        String title = addTitle.getText();
        String author = addAuthor.getText();
        String description = addDesc.getText();

        if (title.isEmpty() || author.isEmpty() || description.isEmpty() || selectedImageFile == null) {
            showAlert("Erro", "Todos os campos devem ser preenchidos e uma imagem deve ser selecionada.");
            return;
        }

        String imageFileName = title + getFileExtension(selectedImageFile);

        Book book = new Book(title, author, description, imageFileName);
        Gson gson = new Gson();
        String json = gson.toJson(book);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost("http://localhost:8081/books/admin");

            // Read the token from the file
            String token = Files.readString(Paths.get("info.txt")).split(",")[0];
            uploadFile.setHeader("Authorization", "Bearer " + token);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("book", json, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("image", new FileInputStream(selectedImageFile), ContentType.APPLICATION_OCTET_STREAM, imageFileName);

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity, "UTF-8");

            if (response.getStatusLine().getStatusCode() == 201) {
                showAlert("Sucesso", "Livro adicionado com sucesso.");
                addTitle.setText("");
                addAuthor.setText("");
                addDesc.setText("");
                imageNameLabel.setText("Nenhuma imagem selecionada");
            } else {
                showAlert("Erro", "Erro ao adicionar livro: " + responseString);
            }
        } catch (IOException e) {
            showAlert("Erro", "Erro ao enviar dados: " + e.getMessage());
        }
    }

    @FXML
    private void fetchBookDetails() {
        String id = editID.getText();
        if (id.isEmpty()) {
            showAlert("Erro", "ID do livro deve ser preenchido.");
            return;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:8081/books/" + id);
            String token = Files.readString(Paths.get("info.txt")).split(",")[0];
            request.setHeader("Authorization", "Bearer " + token);

            CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                JsonObject bookJson = JsonParser.parseString(jsonResponse).getAsJsonObject();

                if (bookJson.has("title")) {
                    editTitle.setText(bookJson.get("title").getAsString());
                }
                if (bookJson.has("author")) {
                    editAuthor.setText(bookJson.get("author").getAsString());
                }
                if (bookJson.has("description")) {
                    editDesc.setText(bookJson.get("description").getAsString());
                }
                if (bookJson.has("imageData")) {
                    String base64Image = bookJson.get("imageData").getAsString();
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    File tempImageFile = File.createTempFile("book_image", ".png");
                    try (FileOutputStream fos = new FileOutputStream(tempImageFile)) {
                        fos.write(imageBytes);
                    }
                    selectedImageFile = tempImageFile;
                    imageNameLabel.setText(tempImageFile.getName());
                }
            } else {
                showAlert("Erro", "Livro não encontrado.");
            }
        } catch (IOException e) {
            showAlert("Erro", "Erro ao buscar dados: " + e.getMessage());
        }
    }

    @FXML
    private void fetchBookDetailsForDeletion() {
        String id = deleteID.getText();
        if (id.isEmpty()) {
            showAlert("Erro", "ID do livro deve ser preenchido.");
            return;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:8081/books/" + id);
            String token = Files.readString(Paths.get("info.txt")).split(",")[0];
            request.setHeader("Authorization", "Bearer " + token);

            CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                JsonObject bookJson = JsonParser.parseString(jsonResponse).getAsJsonObject();

                if (bookJson.has("title")) {
                    deleteTitleText.setText(bookJson.get("title").getAsString());
                }
                if (bookJson.has("author")) {
                    deleteAuthorText.setText(bookJson.get("author").getAsString());
                }
                if (bookJson.has("description")) {
                    deleteDescText.setText(bookJson.get("description").getAsString());
                }
            } else {
                showAlert("Erro", "Livro não encontrado.");
            }
        } catch (IOException e) {
            showAlert("Erro", "Erro ao buscar dados: " + e.getMessage());
        }
    }

    @FXML
    private void updateBook() {
        int id = Integer.parseInt(editID.getText());
        String title = editTitle.getText();
        String author = editAuthor.getText();
        String description = editDesc.getText();
        String imageName = imageNameLabel.getText();

        if (id <= 0 || editID.getText().isEmpty() || title.isEmpty() || author.isEmpty() || description.isEmpty()) {
            showAlert("Erro", "Todos os campos devem ser preenchidos.");
            return;
        }

        String imageFileName = selectedImageFile != null ? title + getFileExtension(selectedImageFile) : imageName;

        Book book = new Book(id, title, author, description, imageFileName);
        Gson gson = new Gson();
        String json = gson.toJson(book);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut updateFile = new HttpPut("http://localhost:8081/books/admin");

            String token = Files.readString(Paths.get("info.txt")).split(",")[0];
            updateFile.setHeader("Authorization", "Bearer " + token);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("book", json, ContentType.APPLICATION_JSON);
            if (selectedImageFile != null) {
                builder.addBinaryBody("image", new FileInputStream(selectedImageFile), ContentType.APPLICATION_OCTET_STREAM, imageFileName);
            }

            HttpEntity multipart = builder.build();
            updateFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(updateFile);

            if (response.getStatusLine().getStatusCode() == 204) {
                showAlert("Sucesso", "Livro atualizado com sucesso.");
                editID.setText("");
                editTitle.setText("");
                editAuthor.setText("");
                editDesc.setText("");
                imageNameLabel.setText("Nenhuma imagem selecionada");
            } else {
                showAlert("Erro", "Erro ao atualizar livro: ");
            }
        } catch (IOException e) {
            showAlert("Erro", "Erro ao enviar dados: " + e.getMessage());
        }
    }

    @FXML
    private void deleteBook() {
        String id = deleteID.getText();
        if (id.isEmpty()) {
            showAlert("Erro", "ID do livro deve ser preenchido.");
            return;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete deleteRequest = new HttpDelete("http://localhost:8081/books/admin/" + id);
            String token = Files.readString(Paths.get("info.txt")).split(",")[0];
            deleteRequest.setHeader("Authorization", "Bearer " + token);

            CloseableHttpResponse response = httpClient.execute(deleteRequest);
            if (response.getStatusLine().getStatusCode() == 204) {
                showAlert("Sucesso", "Livro deletado com sucesso.");
                deleteID.setText("");
                deleteTitleText.setText("");
                deleteAuthorText.setText("");
                deleteDescText.setText("");
            } else {
                showAlert("Erro", "Erro ao deletar livro.");
            }
        } catch (IOException e) {
            showAlert("Erro", "Erro ao enviar dados: " + e.getMessage());
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package org.registry.akashic.akashicjavafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.google.gson.Gson;
import java.util.Map;

public class ProfileController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label roleLabel;

    public void setUserInfo(String userInfo) {
        Gson gson = new Gson();
        Map<String, String> userMap = gson.fromJson(userInfo, Map.class);
        usernameLabel.setText(userMap.get("username"));
        nameLabel.setText(userMap.get("name"));
        roleLabel.setText("ROLE_USER".equals(userMap.get("role")) ? "Usuário" : "Admin");

    }
}
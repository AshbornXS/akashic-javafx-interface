module org.registry.akashic.akashicjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;

    opens org.registry.akashic.akashicjavafx to javafx.fxml, com.google.gson;
    exports org.registry.akashic.akashicjavafx;
    exports org.registry.akashic.akashicjavafx.controller;
    opens org.registry.akashic.akashicjavafx.controller to com.google.gson, javafx.fxml;
    exports org.registry.akashic.akashicjavafx.response;
    opens org.registry.akashic.akashicjavafx.response to com.google.gson, javafx.fxml;
}
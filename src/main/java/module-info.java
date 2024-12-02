module org.registry.akashic.akashicjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;

    opens org.registry.akashic.akashicjavafx to javafx.fxml, com.google.gson;
    exports org.registry.akashic.akashicjavafx;
}
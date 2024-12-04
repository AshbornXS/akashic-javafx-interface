module org.registry.akashic.akashicjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpmime;

    opens org.registry.akashic.akashicjavafx.controller to javafx.fxml;
    opens org.registry.akashic.akashicjavafx.domain to com.google.gson;

    exports org.registry.akashic.akashicjavafx;
    exports org.registry.akashic.akashicjavafx.controller;

    opens org.registry.akashic.akashicjavafx to javafx.fxml, com.google.gson;
    exports org.registry.akashic.akashicjavafx.response;
    opens org.registry.akashic.akashicjavafx.response to com.google.gson, javafx.fxml;
}
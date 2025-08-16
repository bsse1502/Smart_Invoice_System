module org.example.smartinvoicesystem {
    // JavaFX core modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Java standard modules
    requires java.sql;
    requires java.desktop;

    // Third-party JavaFX libraries
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    // Open packages that contain FXML controllers or bound models
    opens org.example.smartinvoicesystem to javafx.fxml;
    opens org.example.smartinvoicesystem.controller to javafx.fxml;
    opens org.example.smartinvoicesystem.models to javafx.base;

    // Export necessary packages for external or runtime access
    exports org.example.smartinvoicesystem;
    exports org.example.smartinvoicesystem.controller;
    exports org.example.smartinvoicesystem.models;


}

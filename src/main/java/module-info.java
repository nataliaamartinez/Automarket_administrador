module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;

    opens com.example to javafx.fxml;
    opens com.example.Modelo to javafx.base; // <-- esta línea es crucial

    exports com.example;
}



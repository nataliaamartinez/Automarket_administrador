module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example to javafx.fxml;
    opens com.example.Modelo to javafx.base; // <-- esta línea es crucial

    exports com.example;
}



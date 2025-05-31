package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            

            // Cargar el FXML
            URL fxmlResource = getClass().getResource("/com/example/login.fxml");
            if (fxmlResource == null) {
                System.err.println("No se encontró login.fxml");
                return;
            }

            Parent root = FXMLLoader.load(fxmlResource);
            Scene scene = new Scene(root);

    

            // Mostrar ventana
            stage.setTitle("AutoMarket - Login");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

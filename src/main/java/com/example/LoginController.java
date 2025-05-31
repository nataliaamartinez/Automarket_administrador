package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField contrasenaField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button toggleVisibilityButton;

    @FXML
    private Label statusLabel;

    private boolean isPasswordVisible = false;

    @FXML
    public void acceder() {
        String user = usuarioField.getText().trim();
        String password = isPasswordVisible
                ? visiblePasswordField.getText().trim()
                : contrasenaField.getText().trim();

        if (user.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Por favor completa todos los campos.");
            return;
        }

        if (user.equals("admin") && password.equals("12345")) {
            statusLabel.setText("¡Bienvenido, administrador!");

            try {
                URL fxmlUrl = getClass().getResource("/com/example/pantallaPrincipal.fxml");
                if (fxmlUrl == null) {
                    statusLabel.setText("No se encontró pantallaPrincipal.fxml");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();

                Scene scene = new Scene(root);

        

                Stage stage = (Stage) usuarioField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Pantalla Principal");
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("No se pudo cargar la pantalla principal.");
            }
        } else {
            statusLabel.setText("Credenciales incorrectas.");
        }
    }

    @FXML
    public void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            visiblePasswordField.setText(contrasenaField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            contrasenaField.setVisible(false);
            contrasenaField.setManaged(false);
            toggleVisibilityButton.setText("🙈");
        } else {
            contrasenaField.setText(visiblePasswordField.getText());
            contrasenaField.setVisible(true);
            contrasenaField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            toggleVisibilityButton.setText("👁");
        }
    }

    @FXML
    private void salir() {
        Stage stage = (Stage) usuarioField.getScene().getWindow();
        stage.close();
    }
}

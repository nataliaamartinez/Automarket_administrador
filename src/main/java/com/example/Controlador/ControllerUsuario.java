package com.example.Controlador;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.example.Modelo.Usuario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ControllerUsuario {

    private final Connection connection;
     private final TableView<Usuario> tableUsuario;

    public ControllerUsuario(Connection connection, TableView<Usuario> tableUsuario) {
        this.connection = connection;
        this.tableUsuario = tableUsuario;
    }

    public void mostrarFormularioUsuario(Usuario usuario, Runnable onSuccess) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle(usuario == null ? "Agregar Usuario" : "Editar Usuario");

        TextField nombreField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        if (usuario != null) {
            nombreField.setText(usuario.getNombre());
            emailField.setText(usuario.getEmail());
            passwordField.setText(usuario.getContrasenia());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(passwordField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Usuario(
                    usuario != null ? usuario.getId() : 0,
                    nombreField.getText(),
                    emailField.getText(),
                    passwordField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            if (usuario == null) {
                agregarUsuario(u);
            } else {
                actualizarUsuario(u);
            }
            onSuccess.run();
        });
    }

    public void agregarUsuario(Usuario u) {
        String sql = "INSERT INTO usuario (nombre, email, contrasenia) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getEmail());
            stmt.setString(3, u.getContrasenia());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar usuario: " + e.getMessage());
        }
    }

    public void actualizarUsuario(Usuario u) {
        String sql = "UPDATE usuario SET nombre = ?, email = ?, contrasenia = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getEmail());
            stmt.setString(3, u.getContrasenia());
            stmt.setInt(4, u.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar usuario: " + e.getMessage());
        }
    }

    public void eliminarUsuario(Usuario u, Runnable onSuccess) {
    if (u == null || u.getId() == null) {
        mostrarAlerta("El usuario a eliminar no es válido.");
        return;
    }

    // Alerta de confirmación
    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
    confirmacion.setTitle("Confirmar eliminación");
    confirmacion.setHeaderText("¿Estás seguro que quieres eliminar este usuario?");
    confirmacion.setContentText("Se eliminarán también todos los anuncios y vehículos asociados a este usuario.");

    Optional<ButtonType> resultado = confirmacion.showAndWait();
    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
        // Si el usuario confirma, se procede a eliminar
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, u.getId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                onSuccess.run();
            } else {
                mostrarAlerta("No se encontró el usuario con ID: " + u.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar usuario: " + e.getMessage());
        }
    } else {
        mostrarAlerta("Eliminación cancelada.");
    }
}

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

      public void cargarTablaUsuario() {
        ObservableList<Usuario> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM usuario";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("contrasenia")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar usuarios: " + e.getMessage());
        }
        tableUsuario.setItems(lista);
    }
}


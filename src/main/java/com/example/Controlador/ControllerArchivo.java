package com.example.Controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.Modelo.Archivo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert.AlertType;

public class ControllerArchivo {

    private final Connection connection;
    private final TableView<Archivo> tableArchivo;
        private  Alertas alertas = new Alertas();


    public ControllerArchivo(Connection connection, TableView<Archivo> tableArchivo) {
        this.connection = connection;
        this.tableArchivo = tableArchivo;
    }

    public void mostrarFormularioArchivo(Archivo archivo, Runnable onSuccess) {
        Dialog<Archivo> dialog = new Dialog<>();
        dialog.setTitle(archivo == null ? "Agregar Archivo" : "Editar Archivo");

        TextField archivoPathField = new TextField();
        archivoPathField.setPromptText("Ruta del archivo (ej: C:/archivos/archivo.pdf)");

        if (archivo != null) {
            archivoPathField.setText(archivo.getArchivoPath());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Ruta del Archivo:"), 0, 0);
        grid.add(archivoPathField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (archivoPathField.getText().trim().isEmpty()) {
                event.consume();
                alertas.mostrarAlerta("La ruta del archivo no puede estar vacía.");
            }
        });

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Archivo(
                    archivo != null ? archivo.getId() : 0,
                    archivoPathField.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            if (archivo == null) {
                agregarArchivo(a);
            } else {
                actualizarArchivo(a);
            }
            onSuccess.run();
        });
    }

    private void agregarArchivo(Archivo archivo) {
        String sql = "INSERT INTO archivo (archivo_path) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, archivo.getArchivoPath());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            alertas.mostrarAlerta("Error al agregar archivo: " + e.getMessage());
        }
    }

    private void actualizarArchivo(Archivo archivo) {
        String sql = "UPDATE archivo SET archivo_path = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, archivo.getArchivoPath());
            stmt.setInt(2, archivo.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            alertas.mostrarAlerta("Error al actualizar archivo: " + e.getMessage());
        }
    }

    public void eliminarArchivo(Archivo archivo, Runnable onSuccess) {
        String sql = "DELETE FROM archivo WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, archivo.getId());
            stmt.executeUpdate();
            onSuccess.run();
        } catch (SQLException e) {
            e.printStackTrace();
            alertas.mostrarAlerta("Error al eliminar archivo: " + e.getMessage());
        }
    }

    public void cargarTablaArchivo() {
        ObservableList<Archivo> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM archivo";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Archivo(rs.getInt("id"), rs.getString("archivo_path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alertas.mostrarAlerta("Error al cargar archivos: " + e.getMessage());
        }
        tableArchivo.setItems(lista);
    }
}

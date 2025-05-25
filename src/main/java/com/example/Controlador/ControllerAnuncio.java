package com.example.Controlador;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.Modelo.Anuncio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ControllerAnuncio {

    private final Connection connection ;
    private final TableView<Anuncio> tableAnuncio;

    public ControllerAnuncio(Connection connection, TableView<Anuncio> tableAnuncio) {
            this.connection = connection;
            this.tableAnuncio = tableAnuncio;
        }

    public void mostrarFormularioAnuncio(Anuncio anuncio, Runnable onSuccess) {
        Dialog<Anuncio> dialog = new Dialog<>();
        dialog.setTitle(anuncio == null ? "Agregar Anuncio" : "Editar Anuncio");

        TextField vehiculoIdField = new TextField();
        TextField precioField = new TextField();
        TextArea descripcionArea = new TextArea();
        TextField vendedorIdField = new TextField();
        TextField archivoIdField = new TextField();

        if (anuncio != null) {
            vehiculoIdField.setText(String.valueOf(anuncio.getVehiculoId()));
            precioField.setText(String.valueOf(anuncio.getPrecio()));
            descripcionArea.setText(anuncio.getDescripcion());
            vendedorIdField.setText(String.valueOf(anuncio.getVendedorId()));
            archivoIdField.setText(anuncio.getArchivoId() != null ? String.valueOf(anuncio.getArchivoId()) : "");
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Vehículo ID:"), 0, 0);
        grid.add(vehiculoIdField, 1, 0);
        grid.add(new Label("Precio:"), 0, 1);
        grid.add(precioField, 1, 1);
        grid.add(new Label("Descripción:"), 0, 2);
        grid.add(descripcionArea, 1, 2);
        grid.add(new Label("Vendedor ID:"), 0, 3);
        grid.add(vendedorIdField, 1, 3);
        grid.add(new Label("Archivo ID (opcional):"), 0, 4);
        grid.add(archivoIdField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Anuncio(
                        anuncio != null ? anuncio.getId() : 0,
                        Integer.parseInt(vehiculoIdField.getText()),
                        Double.parseDouble(precioField.getText()),
                        descripcionArea.getText(),
                        Integer.parseInt(vendedorIdField.getText()),
                        archivoIdField.getText().isEmpty() ? null : Integer.parseInt(archivoIdField.getText())
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Campos numéricos inválidos.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            if (anuncio == null) {
                agregarAnuncio(a);
            } else {
                actualizarAnuncio(a);
            }
            onSuccess.run(); // callback para refrescar UI desde la pantalla principal
        });
    }

    public void agregarAnuncio(Anuncio a) {
        String sql = "INSERT INTO anuncio (vehiculoId, precio, descripcion, vendedorId, archivoId) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, a.getVehiculoId());
            stmt.setDouble(2, a.getPrecio());
            stmt.setString(3, a.getDescripcion());
            stmt.setInt(4, a.getVendedorId());
            if (a.getArchivoId() != null) {
                stmt.setInt(5, a.getArchivoId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar anuncio: " + e.getMessage());
        }
    }

    public void actualizarAnuncio(Anuncio a) {
        String sql = "UPDATE anuncio SET vehiculoId = ?, precio = ?, descripcion = ?, vendedorId = ?, archivoId = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, a.getVehiculoId());
            stmt.setDouble(2, a.getPrecio());
            stmt.setString(3, a.getDescripcion());
            stmt.setInt(4, a.getVendedorId());
            if (a.getArchivoId() != null) {
                stmt.setInt(5, a.getArchivoId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, a.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar anuncio: " + e.getMessage());
        }
    }

    public void eliminarAnuncio(Anuncio anuncio, Runnable onSuccess) {
        if (anuncio == null || anuncio.getId() == null) {
            mostrarAlerta("El anuncio a eliminar no es válido.");
            return;
        }

        String sql = "DELETE FROM anuncio WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, anuncio.getId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                onSuccess.run(); // refrescar tabla
            } else {
                mostrarAlerta("No se encontró el anuncio con ID: " + anuncio.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar anuncio: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void cargarTablaAnuncio() {
        ObservableList<Anuncio> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM anuncio";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Integer archivoId = rs.getObject("archivo_id") != null ? rs.getInt("archivo_id") : null;
                lista.add(new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id"),
                    archivoId
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
           
            mostrarAlerta("Error al cargar anuncios: " + e.getMessage());
        }
        tableAnuncio.setItems(lista);
    }

}

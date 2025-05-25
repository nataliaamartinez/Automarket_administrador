package com.example.Controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.Modelo.Moto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ControllerMoto {
    private final Connection connection;
    private final TableView<Moto> tableMoto;

    public ControllerMoto(Connection connection, TableView<Moto> tableMoto) {
        this.connection = connection;
        this.tableMoto = tableMoto;
    }

   public void cargarTablaMoto() {
    ObservableList<Moto> lista = FXCollections.observableArrayList();
    String sql = "SELECT id, cilindrada FROM moto";  // Solo estos campos existen
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            Moto m = new Moto(
                rs.getInt("id"),
                rs.getInt("cilindrada")
            );
            lista.add(m);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al cargar motos: " + e.getMessage());
    }
    tableMoto.setItems(lista);
}

    public void mostrarFormularioMoto(Moto moto) {
        Dialog<Moto> dialog = new Dialog<>();
        dialog.setTitle(moto == null ? "Agregar Moto" : "Editar Moto");

        TextField marcaField = new TextField();
        TextField modeloField = new TextField();
        TextField anioField = new TextField();
        TextField kilometrajeField = new TextField();
        TextField usuarioIdField = new TextField();
        TextField cilindradaField = new TextField();

        if (moto != null) {
            marcaField.setText(moto.getMarca());
            modeloField.setText(moto.getModelo());
            anioField.setText(String.valueOf(moto.getAnio()));
            kilometrajeField.setText(String.valueOf(moto.getKilometraje()));
            usuarioIdField.setText(String.valueOf(moto.getUsuarioId()));
            cilindradaField.setText(String.valueOf(moto.getCilindrada()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Marca:"), 0, 0);
        grid.add(marcaField, 1, 0);
        grid.add(new Label("Modelo:"), 0, 1);
        grid.add(modeloField, 1, 1);
        grid.add(new Label("Año:"), 0, 2);
        grid.add(anioField, 1, 2);
        grid.add(new Label("Kilometraje:"), 0, 3);
        grid.add(kilometrajeField, 1, 3);
        grid.add(new Label("Usuario ID:"), 0, 4);
        grid.add(usuarioIdField, 1, 4);
        grid.add(new Label("Cilindrada:"), 0, 5);
        grid.add(cilindradaField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                // Validar campos obligatorios
                if (marcaField.getText().trim().isEmpty() || modeloField.getText().trim().isEmpty() ||
                    anioField.getText().trim().isEmpty() || kilometrajeField.getText().trim().isEmpty() ||
                    usuarioIdField.getText().trim().isEmpty() || cilindradaField.getText().trim().isEmpty()) {
                    mostrarAlerta("Todos los campos son obligatorios.");
                    return null;
                }

                try {
                    return new Moto(
                        moto != null ? moto.getId() : 0,
                        marcaField.getText().trim(),
                        modeloField.getText().trim(),
                        parseEntero(anioField.getText().trim(), "Año"),
                        parseEntero(kilometrajeField.getText().trim(), "Kilometraje"),
                        parseEntero(usuarioIdField.getText().trim(), "Usuario ID"),
                        parseEntero(cilindradaField.getText().trim(), "Cilindrada")
                    );
                } catch (NumberFormatException e) {
                    // El error se muestra en parseEntero, aquí solo retornamos null para no crear Moto
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(m -> {
            if (m == null) return; // Si hubo error de validación o cancelación no hacer nada

            if (moto == null) {
                agregarMoto(m);
            } else {
                actualizarMoto(m);
            }
            cargarTablaMoto();
        });
    }

    private int parseEntero(String texto, String campo) throws NumberFormatException {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            mostrarAlerta("El campo \"" + campo + "\" debe ser un número entero válido.");
            throw e;
        }
    }

    public void agregarMoto(Moto m) {
        String sql = "INSERT INTO moto (marca, modelo, anio, kilometraje, usuarioId, cilindrada) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, m.getMarca());
            stmt.setString(2, m.getModelo());
            stmt.setInt(3, m.getAnio());
            stmt.setInt(4, m.getKilometraje());
            stmt.setInt(5, m.getUsuarioId());
            stmt.setInt(6, m.getCilindrada());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar moto: " + e.getMessage());
        }
    }

    public void actualizarMoto(Moto m) {
        String sql = "UPDATE moto SET marca = ?, modelo = ?, anio = ?, kilometraje = ?, usuarioId = ?, cilindrada = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, m.getMarca());
            stmt.setString(2, m.getModelo());
            stmt.setInt(3, m.getAnio());
            stmt.setInt(4, m.getKilometraje());
            stmt.setInt(5, m.getUsuarioId());
            stmt.setInt(6, m.getCilindrada());
            stmt.setInt(7, m.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar moto: " + e.getMessage());
        }
    }

    public void eliminarMoto(Moto moto) {
        String sql = "DELETE FROM moto WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, moto.getId());
            stmt.executeUpdate();
            cargarTablaMoto();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar moto: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

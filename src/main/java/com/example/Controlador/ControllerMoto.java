package com.example.Controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.Modelo.Moto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

    // Carga completa de motos, uniendo vehiculo + moto
    public void cargarTablaMoto() {
       ObservableList<Moto> lista = FXCollections.observableArrayList();
String sql = "SELECT v.id, m.cilindrada FROM vehiculo v JOIN moto m ON v.id = m.id";

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
            anioField.setText(String.valueOf(moto.getAño()));
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
                // Validar campos no vacíos
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
                        Integer.parseInt(anioField.getText().trim()),
                        Integer.parseInt(kilometrajeField.getText().trim()),
                        Integer.parseInt(usuarioIdField.getText().trim()),
                        Integer.parseInt(cilindradaField.getText().trim())
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Por favor, ingrese valores numéricos válidos.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(m -> {
            if (m == null) return;

            if (moto == null) {
                agregarMoto(m);
            } else {
                actualizarMoto(m);
            }
            cargarTablaMoto();
        });
    }

    // Agregar moto: Insert en vehiculo y luego en moto con id generado
    public void agregarMoto(Moto m) {
        String sqlVehiculo = "INSERT INTO vehiculo (marca, modelo, anio, kilometraje, usuarioId) VALUES (?, ?, ?, ?, ?)";
        String sqlMoto = "INSERT INTO moto (id, cilindrada) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo, Statement.RETURN_GENERATED_KEYS)) {
                stmtVeh.setString(1, m.getMarca());
                stmtVeh.setString(2, m.getModelo());
                stmtVeh.setInt(3, m.getAño());
                stmtVeh.setInt(4, m.getKilometraje());
                stmtVeh.setInt(5, m.getUsuarioId());
                stmtVeh.executeUpdate();

                ResultSet rsKeys = stmtVeh.getGeneratedKeys();
                if (rsKeys.next()) {
                    int idGenerado = rsKeys.getInt(1);

                    try (PreparedStatement stmtMoto = connection.prepareStatement(sqlMoto)) {
                        stmtMoto.setInt(1, idGenerado);
                        stmtMoto.setInt(2, m.getCilindrada());
                        stmtMoto.executeUpdate();
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            mostrarAlerta("Error al agregar moto: " + e.getMessage());
        }
    }

    // Actualizar moto y vehiculo
    public void actualizarMoto(Moto m) {
        String sqlVehiculo = "UPDATE vehiculo SET marca = ?, modelo = ?, anio = ?, kilometraje = ?, usuarioId = ? WHERE id = ?";
        String sqlMoto = "UPDATE moto SET cilindrada = ? WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo)) {
                stmtVeh.setString(1, m.getMarca());
                stmtVeh.setString(2, m.getModelo());
                stmtVeh.setInt(3, m.getAño());
                stmtVeh.setInt(4, m.getKilometraje());
                stmtVeh.setInt(5, m.getUsuarioId());
                stmtVeh.setInt(6, m.getId());
                stmtVeh.executeUpdate();
            }

            try (PreparedStatement stmtMoto = connection.prepareStatement(sqlMoto)) {
                stmtMoto.setInt(1, m.getCilindrada());
                stmtMoto.setInt(2, m.getId());
                stmtMoto.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            mostrarAlerta("Error al actualizar moto: " + e.getMessage());
        }
    }

    // Eliminar moto: Primero eliminar de moto, luego de vehiculo
    public void eliminarMoto(Moto moto) {
        String sqlMoto = "DELETE FROM moto WHERE id = ?";
        String sqlVehiculo = "DELETE FROM vehiculo WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtMoto = connection.prepareStatement(sqlMoto)) {
                stmtMoto.setInt(1, moto.getId());
                stmtMoto.executeUpdate();
            }

            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo)) {
                stmtVeh.setInt(1, moto.getId());
                stmtVeh.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
            cargarTablaMoto();
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
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

    @FXML
    private void agregarMoto() {
        mostrarFormularioMoto(null);
    }

    @FXML
    private void editarMoto() {
        Moto seleccionado = tableMoto.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione una moto para editar.");
            return;
        }
        mostrarFormularioMoto(seleccionado);
    }

    @FXML
    private void eliminarMoto() {
        Moto seleccionado = tableMoto.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione una moto para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro que desea eliminar la moto?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                eliminarMoto(seleccionado);
            }
        });
    }
}

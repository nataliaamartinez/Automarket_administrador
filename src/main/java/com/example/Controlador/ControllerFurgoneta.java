package com.example.Controlador;


import com.example.Modelo.Furgoneta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;

public class ControllerFurgoneta {
    

    private final Connection connection;
    private final TableView<Furgoneta> tableFurgoneta;

    public ControllerFurgoneta(Connection connection, TableView<Furgoneta> tableFurgoneta) {
        this.connection = connection;
        this.tableFurgoneta = tableFurgoneta;
    }

    

    public void mostrarFormularioFurgoneta(Furgoneta furgoneta) {
        Dialog<Furgoneta> dialog = new Dialog<>();
        dialog.setTitle(furgoneta == null ? "Agregar Furgoneta" : "Editar Furgoneta");

        TextField marcaField = new TextField();
        TextField modeloField = new TextField();
        TextField anioField = new TextField();
        TextField kilometrajeField = new TextField();
        TextField usuarioIdField = new TextField();
        TextField capacidadCargaField = new TextField();

        if (furgoneta != null) {
            marcaField.setText(furgoneta.getMarca());
            modeloField.setText(furgoneta.getModelo());
            anioField.setText(String.valueOf(furgoneta.getAño()));
            kilometrajeField.setText(String.valueOf(furgoneta.getKilometraje()));
            usuarioIdField.setText(String.valueOf(furgoneta.getUsuarioId()));
            capacidadCargaField.setText(String.valueOf(furgoneta.getCapacidadcarga()));
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
        grid.add(new Label("Capacidad de carga:"), 0, 5);
        grid.add(capacidadCargaField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    Integer anio = Integer.parseInt(anioField.getText());
                    Integer kilometraje = Integer.parseInt(kilometrajeField.getText());
                    Integer usuarioId = Integer.parseInt(usuarioIdField.getText());
                    Double capacidadCarga = Double.parseDouble(capacidadCargaField.getText());

                    return new Furgoneta(
                        furgoneta != null ? furgoneta.getId() : 0,
                        marcaField.getText(),
                        modeloField.getText(),
                        anio,
                        kilometraje,
                        usuarioId,
                        capacidadCarga
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Verifica los campos numéricos.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(f -> {
            if (furgoneta == null) {
                agregarFurgoneta(f);
            } else {
                actualizarFurgoneta(f);
            }
            cargarTablaFurgoneta();
        });
    }

    public void cargarTablaFurgoneta() {
    ObservableList<Furgoneta> lista = FXCollections.observableArrayList();
    String sql = "SELECT id, capacidadCarga FROM furgoneta";

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            Furgoneta furgoneta = new Furgoneta(
                rs.getInt("id"),
                 null,    // marca (no existe en tabla coche)
                null,    // modelo (no existe)
                null,    // anio (no existe)
                null,    // kilometraje (no existe)
                null,  
                rs.getDouble("capacidadCarga")
            );
            lista.add(furgoneta);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al cargar furgonetas: " + e.getMessage());
    }
    tableFurgoneta.setItems(lista);
}



public void agregarFurgoneta(Furgoneta f) {
    // Insert en vehiculo
    String sqlVehiculo = "INSERT INTO vehiculo (marca, modelo, anio, kilometraje, usuarioId) VALUES (?, ?, ?, ?, ?)";
    // Luego obtener el id generado para usarlo en furgoneta
    String sqlFurgoneta = "INSERT INTO furgoneta (id, capacidadCarga) VALUES (?, ?)";
    try {
        connection.setAutoCommit(false); // Transacción

        try (PreparedStatement stmtVehiculo = connection.prepareStatement(sqlVehiculo, Statement.RETURN_GENERATED_KEYS)) {
            stmtVehiculo.setString(1, f.getMarca());
            stmtVehiculo.setString(2, f.getModelo());
            stmtVehiculo.setInt(3, f.getAño());
            stmtVehiculo.setInt(4, f.getKilometraje());
            stmtVehiculo.setInt(5, f.getUsuarioId());
            stmtVehiculo.executeUpdate();

            ResultSet rsKeys = stmtVehiculo.getGeneratedKeys();
            if (rsKeys.next()) {
                int generatedId = rsKeys.getInt(1);

                try (PreparedStatement stmtFurgoneta = connection.prepareStatement(sqlFurgoneta)) {
                    stmtFurgoneta.setInt(1, generatedId);
                    stmtFurgoneta.setDouble(2, f.getCapacidadcarga());
                    stmtFurgoneta.executeUpdate();
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
        mostrarAlerta("Error al agregar furgoneta: " + e.getMessage());
    }
}

public void actualizarFurgoneta(Furgoneta f) {
    String sqlVehiculo = "UPDATE vehiculo SET marca = ?, modelo = ?, anio = ?, kilometraje = ?, usuarioId = ? WHERE id = ?";
    String sqlFurgoneta = "UPDATE furgoneta SET capacidadCarga = ? WHERE id = ?";
    try {
        connection.setAutoCommit(false);

        try (PreparedStatement stmtVehiculo = connection.prepareStatement(sqlVehiculo)) {
            stmtVehiculo.setString(1, f.getMarca());
            stmtVehiculo.setString(2, f.getModelo());
            stmtVehiculo.setInt(3, f.getAño());
            stmtVehiculo.setInt(4, f.getKilometraje());
            stmtVehiculo.setInt(5, f.getUsuarioId());
            stmtVehiculo.setInt(6, f.getId());
            stmtVehiculo.executeUpdate();
        }

        try (PreparedStatement stmtFurgoneta = connection.prepareStatement(sqlFurgoneta)) {
            stmtFurgoneta.setDouble(1, f.getCapacidadcarga());
            stmtFurgoneta.setInt(2, f.getId());
            stmtFurgoneta.executeUpdate();
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
        mostrarAlerta("Error al actualizar furgoneta: " + e.getMessage());
    }
}


    public void eliminarFurgoneta(Furgoneta furgoneta) {
        String sql = "DELETE FROM furgoneta WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, furgoneta.getId());
            stmt.executeUpdate();
            cargarTablaFurgoneta();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar furgoneta: " + e.getMessage());
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

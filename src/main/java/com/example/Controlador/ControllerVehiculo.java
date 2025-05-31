package com.example.Controlador;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.Modelo.Vehiculo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ControllerVehiculo {

    private final Connection connection;
     private final TableView<Vehiculo> tableVehiculo;

    public ControllerVehiculo(Connection connection, TableView<Vehiculo> tableVehiculo) {
        this.connection = connection;
        this.tableVehiculo = tableVehiculo;
    }

    public void mostrarFormularioVehiculo(Vehiculo vehiculo, Runnable onSuccess) {
        Dialog<Vehiculo> dialog = new Dialog<>();
        dialog.setTitle(vehiculo == null ? "Agregar Vehículo" : "Editar Vehículo");

        TextField marcaField = new TextField();
        TextField modeloField = new TextField();
        TextField añoField = new TextField();
        TextField kilometrajeField = new TextField();
        TextField usuarioIdField = new TextField();

        if (vehiculo != null) {
            marcaField.setText(vehiculo.getMarca());
            modeloField.setText(vehiculo.getModelo());
            añoField.setText(String.valueOf(vehiculo.getAño()));
            kilometrajeField.setText(String.valueOf(vehiculo.getKilometraje()));
            usuarioIdField.setText(String.valueOf(vehiculo.getUsuarioId()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Marca:"), 0, 0);
        grid.add(marcaField, 1, 0);
        grid.add(new Label("Modelo:"), 0, 1);
        grid.add(modeloField, 1, 1);
        grid.add(new Label("Año:"), 0, 2);
        grid.add(añoField, 1, 2);
        grid.add(new Label("Kilometraje:"), 0, 3);
        grid.add(kilometrajeField, 1, 3);
        grid.add(new Label("Usuario ID:"), 0, 4);
        grid.add(usuarioIdField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Vehiculo(
                        vehiculo != null ? vehiculo.getId() : 0,
                        marcaField.getText(),
                        modeloField.getText(),
                        Integer.parseInt(añoField.getText()),
                        Integer.parseInt(kilometrajeField.getText()),
                        Integer.parseInt(usuarioIdField.getText())
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Por favor ingresa valores numéricos válidos en Año, Kilometraje y Usuario ID.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(v -> {
            if (vehiculo == null) {
                agregarVehiculo(v);
            } else {
                actualizarVehiculo(v);
            }
            onSuccess.run();
        });
    }

    public void agregarVehiculo(Vehiculo v) {
        String sql = "INSERT INTO vehiculo (marca, modelo, año, kilometraje, usuario_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, v.getMarca());
            stmt.setString(2, v.getModelo());
            stmt.setInt(3, v.getAño());
            stmt.setInt(4, v.getKilometraje());
            stmt.setInt(5, v.getUsuarioId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar vehículo: " + e.getMessage());
        }
    }

    public void actualizarVehiculo(Vehiculo v) {
        String sql = "UPDATE vehiculo SET marca = ?, modelo = ?, año = ?, kilometraje = ?, usuario_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, v.getMarca());
            stmt.setString(2, v.getModelo());
            stmt.setInt(3, v.getAño());
            stmt.setInt(4, v.getKilometraje());
            stmt.setInt(5, v.getUsuarioId());
            stmt.setInt(6, v.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar vehículo: " + e.getMessage());
        }
    }

    public void eliminarVehiculo(Vehiculo vehiculo, Runnable onSuccess) {
        if (vehiculo == null || vehiculo.getId() == null) {
            mostrarAlerta("El vehículo a eliminar no es válido.");
            return;
        }

        String sql = "DELETE FROM vehiculo WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehiculo.getId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                onSuccess.run();
            } else {
                mostrarAlerta("No se encontró el vehículo con ID: " + vehiculo.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar vehículo: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

     public void cargarTablaVehiculo() {
        ObservableList<Vehiculo> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM vehiculo";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Vehiculo(
                    rs.getInt("id"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("año"),
                    rs.getInt("kilometraje"),
                    rs.getInt("usuario_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar vehículos: " + e.getMessage());
        }
        tableVehiculo.setItems(lista);
    }

    public List<Vehiculo> buscarVehiculosPorAnioAproximado(int anioBase) {
    List<Vehiculo> resultados = new ArrayList<>();
    String sql = "SELECT * FROM vehiculo WHERE año BETWEEN ? AND ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, anioBase - 2);
        stmt.setInt(2, anioBase + 2);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            resultados.add(new Vehiculo(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar por año: " + e.getMessage());
    }
    return resultados;
}
public List<Vehiculo> buscarVehiculosPorKilometrajeAproximado(int kmBase) {
    List<Vehiculo> resultados = new ArrayList<>();
    String sql = "SELECT * FROM vehiculo WHERE kilometraje BETWEEN ? AND ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, kmBase - 10000);
        stmt.setInt(2, kmBase + 10000);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            resultados.add(new Vehiculo(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar por kilometraje: " + e.getMessage());
    }
    return resultados;
}
public List<Vehiculo> buscarVehiculosPorMarca(String marca) {
    List<Vehiculo> resultados = new ArrayList<>();
    String sql = "SELECT * FROM vehiculo WHERE marca LIKE ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, "%" + marca + "%"); // Búsqueda parcial
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            resultados.add(new Vehiculo(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar por marca: " + e.getMessage());
    }
    return resultados;
}
public List<Vehiculo> buscarVehiculosPorUsuarioId(int usuarioId) {
    List<Vehiculo> resultados = new ArrayList<>();
    String sql = "SELECT * FROM vehiculo WHERE usuario_id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            resultados.add(new Vehiculo(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar por usuario ID: " + e.getMessage());
    }
    return resultados;
}

public void eliminarVehiculosSinUsuario(Runnable onSuccess) {
String sql = "DELETE FROM vehiculo WHERE usuario_id IS NULL OR usuario_id = 0";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        int filasAfectadas = stmt.executeUpdate();
        if (filasAfectadas > 0) {
            onSuccess.run();
        } else {
            mostrarAlerta("No se encontraron vehículos con usuario_id = 0.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al eliminar vehículos sin usuario: " + e.getMessage());
    }
}

    
}


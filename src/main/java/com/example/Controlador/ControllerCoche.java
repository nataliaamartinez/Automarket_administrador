package com.example.Controlador;

import com.example.Modelo.Coche;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;

public class ControllerCoche {

    private final Connection connection;
    private final TableView<Coche> tableCoche;

    public ControllerCoche(Connection connection, TableView<Coche> tableCoche) {
        this.connection = connection;
        this.tableCoche = tableCoche;
    }

   public void cargarTablaCoche() {
    ObservableList<Coche> lista = FXCollections.observableArrayList();
    String sql = "SELECT id, carroceria FROM coche"; // Solo esos dos campos existen
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            Coche c = new Coche(
                rs.getInt("id"),
                null,    // marca (no existe en tabla coche)
                null,    // modelo (no existe)
                null,    // anio (no existe)
                null,    // kilometraje (no existe)
                null,    // usuarioId (no existe)
                rs.getString("carroceria")
            );
            lista.add(c);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al cargar coches: " + e.getMessage());
    }
    tableCoche.setItems(lista);
}


    public void mostrarFormularioCoche(Coche coche, Runnable onSuccess) {
        Dialog<Coche> dialog = new Dialog<>();
        dialog.setTitle(coche == null ? "Agregar Coche" : "Editar Coche");

        TextField marcaField = new TextField();
        TextField modeloField = new TextField();
        TextField añoField = new TextField();
        TextField kilometrajeField = new TextField();
        TextField usuarioIdField = new TextField();
        TextField carroceriaField = new TextField();

        if (coche != null) {
            marcaField.setText(coche.getMarca());
            modeloField.setText(coche.getModelo());
            añoField.setText(String.valueOf(coche.getAño()));
            kilometrajeField.setText(String.valueOf(coche.getKilometraje()));
            usuarioIdField.setText(String.valueOf(coche.getUsuarioId()));
            carroceriaField.setText(coche.getCarroceria());
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
        grid.add(new Label("ID Usuario:"), 0, 4);
        grid.add(usuarioIdField, 1, 4);
        grid.add(new Label("Carrocería:"), 0, 5);
        grid.add(carroceriaField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Coche(
                        coche != null ? coche.getId() : 0,
                        marcaField.getText(),
                        modeloField.getText(),
                        Integer.parseInt(añoField.getText()),
                        Integer.parseInt(kilometrajeField.getText()),
                        Integer.parseInt(usuarioIdField.getText()),
                        carroceriaField.getText()
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Verifica los campos numéricos.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            if (coche == null) {
                agregarCoche(c);
            } else {
                actualizarCoche(c);
            }
            onSuccess.run(); // ✅ Aquí ejecutamos el callback
        });
    }

    public void agregarCoche(Coche c) {
        String sql = "INSERT INTO coche (marca, modelo, año, kilometraje, usuarioId, carroceria) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, c.getMarca());
            stmt.setString(2, c.getModelo());
            stmt.setInt(3, c.getAño());
            stmt.setInt(4, c.getKilometraje());
            stmt.setInt(5, c.getUsuarioId());
            stmt.setString(6, c.getCarroceria());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar coche: " + e.getMessage());
        }
    }

    public void actualizarCoche(Coche c) {
        String sql = "UPDATE coche SET marca = ?, modelo = ?, año = ?, kilometraje = ?, usuarioId = ?, carroceria = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, c.getMarca());
            stmt.setString(2, c.getModelo());
            stmt.setInt(3, c.getAño());
            stmt.setInt(4, c.getKilometraje());
            stmt.setInt(5, c.getUsuarioId());
            stmt.setString(6, c.getCarroceria());
            stmt.setInt(7, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar coche: " + e.getMessage());
        }
    }

    public void eliminarCoche(Coche c, Runnable onSuccess) {
        String sql = "DELETE FROM coche WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, c.getId());
            stmt.executeUpdate();
            onSuccess.run(); // ✅ Aquí ejecutamos el callback
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar coche: " + e.getMessage());
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
private void agregarCoche() {
    mostrarFormularioCoche(null, () -> cargarTablaCoche());
}

@FXML
private void editarCoche() {
    var seleccionado = tableCoche.getSelectionModel().getSelectedItem();
    if (seleccionado == null) {
        mostrarAlerta("Seleccione un coche para editar.");
        return;
    }
    mostrarFormularioCoche(seleccionado, () -> cargarTablaCoche());
}

@FXML
private void eliminarCoche() {
    var seleccionado = tableCoche.getSelectionModel().getSelectedItem();
    if (seleccionado == null) {
        mostrarAlerta("Seleccione un coche para eliminar.");
        return;
    }
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro que desea eliminar el coche?", ButtonType.YES, ButtonType.NO);
    confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.YES) {
            eliminarCoche(seleccionado, () -> cargarTablaCoche());
        }
    });
}
}

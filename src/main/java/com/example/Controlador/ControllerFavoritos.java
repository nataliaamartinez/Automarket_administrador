package com.example.Controlador;

import com.example.Modelo.Favorito;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;

public class ControllerFavoritos {

    private final Connection connection;
    private final TableView<Favorito> tableFavorito;

    public ControllerFavoritos(Connection connection, TableView<Favorito> tableFavorito) {
        this.connection = connection;
        this.tableFavorito = tableFavorito;
    }

    public void cargarTablaFavorito() {
        ObservableList<Favorito> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM favorito";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Favorito f = new Favorito(
                    rs.getInt("id"),
                    rs.getInt("anuncio_id"),
                    rs.getInt("comprador_id")
                );
                lista.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar favoritos: " + e.getMessage());
        }
        tableFavorito.setItems(lista);
    }

    public void mostrarFormularioFavorito(Favorito favorito, Runnable onSuccess) {
        Dialog<Favorito> dialog = new Dialog<>();
        dialog.setTitle(favorito == null ? "Agregar Favorito" : "Editar Favorito");

        TextField anuncioIdField = new TextField();
        TextField compradorIdField = new TextField();

        if (favorito != null) {
            anuncioIdField.setText(String.valueOf(favorito.getAnuncioId()));
            compradorIdField.setText(String.valueOf(favorito.getCompradorId()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("ID del Anuncio:"), 0, 0);
        grid.add(anuncioIdField, 1, 0);
        grid.add(new Label("ID del Comprador:"), 0, 1);
        grid.add(compradorIdField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Favorito(
                        favorito != null ? favorito.getId() : 0,
                        Integer.parseInt(anuncioIdField.getText().trim()),
                        Integer.parseInt(compradorIdField.getText().trim())
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Verifica que los campos sean números válidos.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(f -> {
            if (favorito == null) {
                agregarFavorito(f);
            } else {
                actualizarFavorito(f);
            }
            onSuccess.run();
        });
    }

    public void agregarFavorito(Favorito f) {
        String sql = "INSERT INTO favorito (anuncio_id, comprador_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, f.getAnuncioId());
            stmt.setInt(2, f.getCompradorId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar favorito: " + e.getMessage());
        }
    }

    public void actualizarFavorito(Favorito f) {
        String sql = "UPDATE favorito SET anuncio_id = ?, comprador_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, f.getAnuncioId());
            stmt.setInt(2, f.getCompradorId());
            stmt.setInt(3, f.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar favorito: " + e.getMessage());
        }
    }

    public void eliminarFavorito(Favorito favorito, Runnable onSuccess) {
        String sql = "DELETE FROM favorito WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, favorito.getId());
            stmt.executeUpdate();
            onSuccess.run();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar favorito: " + e.getMessage());
        }
    }

    public void mostrarDetalleFavorito(int favoritoId) {
        String sql = """
            SELECT a.id AS anuncio_id, a.precio, a.descripcion,
                   v.marca, v.modelo, v.año, v.kilometraje,
                   c.carroceria, f.capacidadCarga, m.cilindrada
            FROM favorito fav
            JOIN anuncio a ON fav.anuncio_id = a.id
            JOIN vehiculo v ON a.vehiculo_id = v.id
            LEFT JOIN coche c ON v.id = c.id
            LEFT JOIN furgoneta f ON v.id = f.id
            LEFT JOIN moto m ON v.id = m.id
            WHERE fav.id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, favoritoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String tipoExtra = "";
                if (rs.getString("carroceria") != null) {
                    tipoExtra = "Carrocería: " + rs.getString("carroceria") + "\n";
                } else if (rs.getObject("capacidadCarga") != null) {
                    tipoExtra = "Capacidad de Carga: " + rs.getDouble("capacidadCarga") + " kg\n";
                } else if (rs.getObject("cilindrada") != null) {
                    tipoExtra = "Cilindrada: " + rs.getInt("cilindrada") + " cc\n";
                } else {
                    tipoExtra = "Tipo específico no disponible\n";
                }

                StringBuilder info = new StringBuilder();
                info.append("🧡 Favorito ID: ").append(favoritoId).append("\n")
                    .append("Anuncio ID: ").append(rs.getInt("anuncio_id")).append("\n")
                    .append("Precio: €").append(rs.getBigDecimal("precio")).append("\n")
                    .append("Descripción: ").append(rs.getString("descripcion")).append("\n\n")
                    .append("🚗 Vehículo:\n")
                    .append("Marca: ").append(rs.getString("marca")).append("\n")
                    .append("Modelo: ").append(rs.getString("modelo")).append("\n")
                    .append("Año: ").append(rs.getInt("año")).append("\n")
                    .append("Kilometraje: ").append(rs.getInt("kilometraje")).append(" km\n")
                    .append(tipoExtra);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Detalles del Favorito");
                alert.setHeaderText(null);
                alert.setContentText(info.toString());
                alert.getDialogPane().setPrefWidth(500);
                alert.showAndWait();
            } else {
                mostrarAlerta("No se encontró información para el favorito seleccionado.");
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al obtener detalles del favorito: " + e.getMessage());
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
    private void agregarFavorito() {
        mostrarFormularioFavorito(null, this::cargarTablaFavorito);
    }

    @FXML
    private void editarFavorito() {
        var seleccionado = tableFavorito.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un favorito para editar.");
            return;
        }
        mostrarFormularioFavorito(seleccionado, this::cargarTablaFavorito);
    }

    @FXML
    private void eliminarFavorito() {
        var seleccionado = tableFavorito.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un favorito para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro que desea eliminar el favorito?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                eliminarFavorito(seleccionado, this::cargarTablaFavorito);
            }
        });
    }
}

package com.example.Controlador;

import com.example.Modelo.Favorito;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
        grid.add(new Label("ID Anuncio:"), 0, 0);
        grid.add(anuncioIdField, 1, 0);
        grid.add(new Label("ID Comprador:"), 0, 1);
        grid.add(compradorIdField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Favorito(
                        favorito != null ? favorito.getId() : 0,
                        Integer.parseInt(anuncioIdField.getText()),
                        Integer.parseInt(compradorIdField.getText())
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Verifica los campos numéricos.");
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
            onSuccess.run(); // Recargar tabla
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

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

package com.example.Controlador;

import com.example.Modelo.Anuncio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerAnuncio {
    public final Connection connection;
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

        if (anuncio != null) {
            vehiculoIdField.setText(String.valueOf(anuncio.getVehiculoId()));
            precioField.setText(String.valueOf(anuncio.getPrecio()));
            descripcionArea.setText(anuncio.getDescripcion());
            vendedorIdField.setText(String.valueOf(anuncio.getVendedorId()));
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

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Anuncio(
                        anuncio != null ? anuncio.getId() : 0,
                        Integer.parseInt(vehiculoIdField.getText().trim()),
                        Double.parseDouble(precioField.getText().trim()),
                        descripcionArea.getText().trim(),
                        Integer.parseInt(vendedorIdField.getText().trim())
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Campos numéricos inválidos.");
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
            onSuccess.run();
        });
    }

    public void agregarAnuncio(Anuncio a) {
        String sql = "INSERT INTO anuncio (vehiculo_id, precio, descripcion, vendedor_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, a.getVehiculoId());
            stmt.setDouble(2, a.getPrecio());
            stmt.setString(3, a.getDescripcion());
            stmt.setInt(4, a.getVendedorId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar anuncio: " + e.getMessage());
        }
    }

    public void actualizarAnuncio(Anuncio a) {
        String sql = "UPDATE anuncio SET vehiculo_id = ?, precio = ?, descripcion = ?, vendedor_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, a.getVehiculoId());
            stmt.setDouble(2, a.getPrecio());
            stmt.setString(3, a.getDescripcion());
            stmt.setInt(4, a.getVendedorId());
            stmt.setInt(5, a.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar anuncio: " + e.getMessage());
        }
    }

    public void compararAnuncios(int id1, int id2) {
        String sql = """
            SELECT a.id AS anuncio_id, a.precio, a.descripcion,
                   v.marca, v.modelo, v.año, v.kilometraje,
                   u.nombre AS vendedor_nombre, u.email,
                   c.carroceria, f.capacidadCarga, m.cilindrada
            FROM anuncio a
            JOIN vehiculo v ON a.vehiculo_id = v.id
            JOIN usuario u ON a.vendedor_id = u.id
            LEFT JOIN coche c ON v.id = c.id
            LEFT JOIN furgoneta f ON v.id = f.id
            LEFT JOIN moto m ON v.id = m.id
            WHERE a.id = ? OR a.id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id1);
            stmt.setInt(2, id2);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("\uD83D\uDCE2 Anuncio ID: ").append(rs.getInt("anuncio_id"))
                  .append("\nPrecio: €").append(rs.getDouble("precio"))
                  .append("\nDescripción: ").append(rs.getString("descripcion"))
                  .append("\n\n\uD83D\uDE97 Vehículo:")
                  .append("\nMarca: ").append(rs.getString("marca"))
                  .append("\nModelo: ").append(rs.getString("modelo"))
                  .append("\nAño: ").append(rs.getInt("año"))
                  .append("\nKilometraje: ").append(rs.getInt("kilometraje"))
                  .append(" km\n")
                  .append("\n\uD83D\uDC64 Vendedor: ").append(rs.getString("vendedor_nombre"))
                  .append(" (Email: ").append(rs.getString("email")).append(")\n");

                if (rs.getString("carroceria") != null) {
                    sb.append("Tipo: Coche\nCarrocería: ").append(rs.getString("carroceria"));
                } else if (rs.getDouble("capacidadCarga") > 0) {
                    sb.append("Tipo: Furgoneta\nCapacidad de carga: ").append(rs.getDouble("capacidadCarga")).append(" kg");
                } else if (rs.getInt("cilindrada") > 0) {
                    sb.append("Tipo: Moto\nCilindrada: ").append(rs.getInt("cilindrada")).append(" cc");
                } else {
                    sb.append("Tipo: Desconocido");
                }
                sb.append("\n---------------------------\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Comparación de Anuncios");
            alert.setHeaderText(null);
            TextArea area = new TextArea(sb.toString());
            area.setEditable(false);
            area.setWrapText(true);
            alert.getDialogPane().setContent(area);
            alert.getDialogPane().setPrefWidth(600);
            alert.showAndWait();

        } catch (SQLException e) {
            mostrarAlerta("Error al comparar anuncios: " + e.getMessage());
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
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar anuncios: " + e.getMessage());
        }
        tableAnuncio.setItems(lista);
    }

    public ObservableList<Anuncio> buscarPorUsuario(String nombreUsuario) {
        ObservableList<Anuncio> lista = FXCollections.observableArrayList();
        String sql = "SELECT a.* FROM anuncio a JOIN vendedor v ON a.vendedor_id = v.id WHERE v.nombre LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nombreUsuario + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al buscar por usuario: " + e.getMessage());
        }
        return lista;
    }

    public ObservableList<Anuncio> buscarPorPrecio(double precio) {
        ObservableList<Anuncio> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM anuncio WHERE precio = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, precio);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al buscar por precio: " + e.getMessage());
        }
        return lista;
    }

    public ObservableList<Anuncio> buscarPorKilometraje(int kilometros) {
        ObservableList<Anuncio> lista = FXCollections.observableArrayList();
        String sql = "SELECT a.* FROM anuncio a JOIN vehiculo v ON a.vehiculo_id = v.id WHERE v.kilometros BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, kilometros - 1000);
            stmt.setInt(2, kilometros + 1000);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al buscar por kilometraje: " + e.getMessage());
        }
        return lista;
    }

    public Anuncio obtenerAnuncioMasCaro() {
        String sql = "SELECT * FROM anuncio ORDER BY precio DESC LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al obtener el anuncio más caro: " + e.getMessage());
        }
        return null;
    }

    public Anuncio obtenerAnuncioMasBarato() {
        String sql = "SELECT * FROM anuncio ORDER BY precio ASC LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al obtener el anuncio más barato: " + e.getMessage());
        }
        return null;
    }

    @FXML
    private void agregarAnuncio() {
        mostrarFormularioAnuncio(null, this::cargarTablaAnuncio);
    }

    @FXML
    private void editarAnuncio() {
        var seleccionado = tableAnuncio.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un anuncio para editar.");
            return;
        }
        mostrarFormularioAnuncio(seleccionado, this::cargarTablaAnuncio);
    }

    @FXML
    private void eliminarAnuncio() {
        var seleccionado = tableAnuncio.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un anuncio para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro que desea eliminar el anuncio?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                this.eliminarAnuncioPorId(seleccionado.getId(), this::cargarTablaAnuncio);
            }
        });
    }

    public void eliminarAnuncioPorId(int id, Runnable onSuccess) {
        String sql = "DELETE FROM anuncio WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                onSuccess.run();
            } else {
                mostrarAlerta("No se encontró ningún anuncio con ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar anuncio: " + e.getMessage());
        }
    }

    public List<Anuncio> buscarAnunciosPorVendedorId(int vendedorId) {
        List<Anuncio> resultados = new ArrayList<>();
        String sql = "SELECT * FROM anuncio WHERE vendedor_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vendedorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Anuncio a = new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                );
                resultados.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al buscar anuncios: " + e.getMessage());
        }
        return resultados;
    }

    public List<Anuncio> buscarAnunciosPorPrecioAproximado(double precioBase) {
        List<Anuncio> resultados = new ArrayList<>();
        String sql = "SELECT * FROM anuncio WHERE precio BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, precioBase - 1000);
            stmt.setDouble(2, precioBase + 1000);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultados.add(new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al buscar anuncios por precio: " + e.getMessage());
        }
        return resultados;
    }
}

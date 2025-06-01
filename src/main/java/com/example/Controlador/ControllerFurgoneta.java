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

    public void cargarTablaFurgoneta() {
    ObservableList<Furgoneta> lista = FXCollections.observableArrayList();
    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, f.capacidadCarga
        FROM vehiculo v
        JOIN furgoneta f ON v.id = f.id
    """;

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            Furgoneta f = new Furgoneta(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id"),
                rs.getDouble("capacidadCarga")
            );
            lista.add(f);
        }

        tableFurgoneta.setItems(lista);
    } catch (SQLException e) {
        mostrarAlerta("Error al cargar furgonetas: " + e.getMessage());
    }
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
            capacidadCargaField.setText(String.valueOf(furgoneta.getCapacidadCarga()));
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
        grid.add(new Label("Capacidad de carga (kg):"), 0, 5);
        grid.add(capacidadCargaField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    return new Furgoneta(
                        furgoneta != null ? furgoneta.getId() : 0,
                        marcaField.getText(),
                        modeloField.getText(),
                        Integer.parseInt(anioField.getText()),
                        Integer.parseInt(kilometrajeField.getText()),
                        Integer.parseInt(usuarioIdField.getText()),
                        Double.parseDouble(capacidadCargaField.getText())
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

    public void agregarFurgoneta(Furgoneta f) {
        String sqlVehiculo = "INSERT INTO vehiculo (marca, modelo, año, kilometraje, usuario_id) VALUES (?, ?, ?, ?, ?)";
        String sqlFurgoneta = "INSERT INTO furgoneta (id, capacidadCarga) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            int idGenerado;
            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo, Statement.RETURN_GENERATED_KEYS)) {
                stmtVeh.setString(1, f.getMarca());
                stmtVeh.setString(2, f.getModelo());
                stmtVeh.setInt(3, f.getAño());
                stmtVeh.setInt(4, f.getKilometraje());
                stmtVeh.setInt(5, f.getUsuarioId());
                stmtVeh.executeUpdate();

                ResultSet rsKeys = stmtVeh.getGeneratedKeys();
                if (rsKeys.next()) {
                    idGenerado = rsKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID generado.");
                }
            }

            try (PreparedStatement stmtFurgoneta = connection.prepareStatement(sqlFurgoneta)) {
                stmtFurgoneta.setInt(1, idGenerado);
                stmtFurgoneta.setDouble(2, f.getCapacidadCarga());
                stmtFurgoneta.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error al agregar furgoneta: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void actualizarFurgoneta(Furgoneta f) {
        String sqlVehiculo = "UPDATE vehiculo SET marca = ?, modelo = ?, año = ?, kilometraje = ?, usuario_id = ? WHERE id = ?";
        String sqlFurgoneta = "UPDATE furgoneta SET capacidadCarga = ? WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo)) {
                stmtVeh.setString(1, f.getMarca());
                stmtVeh.setString(2, f.getModelo());
                stmtVeh.setInt(3, f.getAño());
                stmtVeh.setInt(4, f.getKilometraje());
                stmtVeh.setInt(5, f.getUsuarioId());
                stmtVeh.setInt(6, f.getId());
                stmtVeh.executeUpdate();
            }

            try (PreparedStatement stmtFur = connection.prepareStatement(sqlFurgoneta)) {
                stmtFur.setDouble(1, f.getCapacidadCarga());
                stmtFur.setInt(2, f.getId());
                stmtFur.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error al actualizar furgoneta: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void eliminarFurgoneta(Furgoneta furgoneta) {
        String sqlFurgoneta = "DELETE FROM furgoneta WHERE id = ?";
        String sqlVehiculo = "DELETE FROM vehiculo WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtF = connection.prepareStatement(sqlFurgoneta)) {
                stmtF.setInt(1, furgoneta.getId());
                stmtF.executeUpdate();
            }

            try (PreparedStatement stmtV = connection.prepareStatement(sqlVehiculo)) {
                stmtV.setInt(1, furgoneta.getId());
                stmtV.executeUpdate();
            }

            connection.commit();
            cargarTablaFurgoneta();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error al eliminar furgoneta: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public ObservableList<Furgoneta> buscarFurgonetasPorUsuario(int usuarioId) {
    ObservableList<Furgoneta> lista = FXCollections.observableArrayList();
    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, f.capacidadCarga,
               u.nombre AS usuario_nombre, u.email AS usuario_email
        FROM vehiculo v
        JOIN furgoneta f ON v.id = f.id
        JOIN usuario u ON v.usuario_id = u.id
        WHERE v.usuario_id = ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();

        StringBuilder info = new StringBuilder();
        while (rs.next()) {
            Furgoneta f = new Furgoneta(
                rs.getInt("id"), rs.getString("marca"), rs.getString("modelo"),
                rs.getInt("año"), rs.getInt("kilometraje"), rs.getInt("usuario_id"),
                rs.getDouble("capacidadCarga")
            );
            lista.add(f);

            info.append("ID: ").append(f.getId()).append("\n")
                .append("Marca: ").append(f.getMarca()).append("\n")
                .append("Modelo: ").append(f.getModelo()).append("\n")
                .append("Año: ").append(f.getAño()).append("\n")
                .append("Kilometraje: ").append(f.getKilometraje()).append(" km\n")
                .append("Capacidad: ").append(f.getCapacidadCarga()).append(" kg\n")
                .append("Usuario: ").append(rs.getString("usuario_nombre")).append(" (")
                .append(rs.getString("usuario_email")).append(")\n\n");
        }

        if (!lista.isEmpty()) {
            mostrarInformacion("Furgonetas del Usuario ID " + usuarioId, info.toString());
        } else {
            mostrarAlerta("No se encontraron furgonetas para el usuario ID: " + usuarioId);
        }

    } catch (SQLException e) {
        mostrarAlerta("Error en la consulta: " + e.getMessage());
    }
    return lista;
}

public ObservableList<Furgoneta> buscarFurgonetasPorCapacidad(double capacidadMin) {
    ObservableList<Furgoneta> lista = FXCollections.observableArrayList();
    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, f.capacidadCarga
        FROM vehiculo v
        JOIN furgoneta f ON v.id = f.id
        WHERE f.capacidadCarga >= ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setDouble(1, capacidadMin);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(new Furgoneta(
                rs.getInt("id"), rs.getString("marca"), rs.getString("modelo"),
                rs.getInt("año"), rs.getInt("kilometraje"), rs.getInt("usuario_id"),
                rs.getDouble("capacidadCarga")
            ));
        }
    } catch (SQLException e) {
        mostrarAlerta("Error al buscar furgonetas por capacidad: " + e.getMessage());
    }
    return lista;
}

public void mostrarAnunciosDeFurgoneta(int id) {
    String sql = "SELECT precio, descripcion FROM anuncio WHERE vehiculo_id = ?";
    StringBuilder contenido = new StringBuilder();

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            contenido.append("- Precio: €").append(rs.getBigDecimal("precio")).append("\n")
                     .append("  Descripción: ").append(rs.getString("descripcion")).append("\n\n");
        }
        if (contenido.length() == 0) contenido.append("Sin anuncios disponibles.");
    } catch (SQLException e) {
        contenido.append("Error: ").append(e.getMessage());
    }

    mostrarInformacion("Anuncios para Furgoneta ID " + id, contenido.toString());
}

public void mostrarInformacionFurgonetaPorId(int id) {
    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, f.capacidadCarga,
               u.nombre AS usuario_nombre, u.email AS usuario_email
        FROM vehiculo v
        JOIN furgoneta f ON v.id = f.id
        JOIN usuario u ON v.usuario_id = u.id
        WHERE v.id = ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            StringBuilder info = new StringBuilder();
            info.append("ID: ").append(rs.getInt("id")).append("\n")
                .append("Marca: ").append(rs.getString("marca")).append("\n")
                .append("Modelo: ").append(rs.getString("modelo")).append("\n")
                .append("Año: ").append(rs.getInt("año")).append("\n")
                .append("Kilometraje: ").append(rs.getInt("kilometraje")).append(" km\n")
                .append("Capacidad: ").append(rs.getDouble("capacidadCarga")).append(" kg\n")
                .append("Usuario: ").append(rs.getString("usuario_nombre"))
                .append(" (").append(rs.getString("usuario_email")).append(")\n");

            mostrarInformacion("Detalle Furgoneta ID: " + id, info.toString());
        } else {
            mostrarAlerta("No se encontró furgoneta con ID: " + id);
        }

    } catch (SQLException e) {
        mostrarAlerta("Error al obtener detalle de furgoneta: " + e.getMessage());
    }
}

private void mostrarInformacion(String titulo, String contenido) {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle(titulo);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

    TextArea textArea = new TextArea(contenido);
    textArea.setWrapText(true);
    textArea.setEditable(false);
    textArea.setPrefWidth(500);
    textArea.setPrefHeight(400);

    ScrollPane scrollPane = new ScrollPane(textArea);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(400);

    dialog.getDialogPane().setContent(scrollPane);
    dialog.showAndWait();
}

}

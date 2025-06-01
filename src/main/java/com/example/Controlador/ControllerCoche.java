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

    // ✅ Cargar todos los coches (JOIN con vehiculo)
    public void cargarTablaCoche() {
        ObservableList<Coche> lista = FXCollections.observableArrayList();
        String sql = """
            SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, c.carroceria
            FROM vehiculo v
            JOIN coche c ON v.id = c.id
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Coche c = new Coche(
                    rs.getInt("id"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("año"),
                    rs.getInt("kilometraje"),
                    rs.getInt("usuario_id"),
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

    // ✅ Formulario para agregar o editar
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
                        marcaField.getText().trim(),
                        modeloField.getText().trim(),
                        Integer.parseInt(añoField.getText().trim()),
                        Integer.parseInt(kilometrajeField.getText().trim()),
                        Integer.parseInt(usuarioIdField.getText().trim()),
                        carroceriaField.getText().trim()
                    );
                } catch (NumberFormatException e) {
                    mostrarAlerta("Verifica que los campos numéricos sean correctos.");
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
            onSuccess.run(); // recargar tabla
        });
    }

    // ✅ Agregar coche (vehiculo + coche)
    public void agregarCoche(Coche c) {
        String sqlVehiculo = "INSERT INTO vehiculo (marca, modelo, año, kilometraje, usuario_id) VALUES (?, ?, ?, ?, ?)";
        String sqlCoche = "INSERT INTO coche (id, carroceria) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            int idGenerado;
            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo, Statement.RETURN_GENERATED_KEYS)) {
                stmtVeh.setString(1, c.getMarca());
                stmtVeh.setString(2, c.getModelo());
                stmtVeh.setInt(3, c.getAño());
                stmtVeh.setInt(4, c.getKilometraje());
                stmtVeh.setInt(5, c.getUsuarioId());
                stmtVeh.executeUpdate();

                ResultSet rs = stmtVeh.getGeneratedKeys();
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID del vehículo.");
                }
            }

            try (PreparedStatement stmtCoche = connection.prepareStatement(sqlCoche)) {
                stmtCoche.setInt(1, idGenerado);
                stmtCoche.setString(2, c.getCarroceria());
                stmtCoche.executeUpdate();
            }

            connection.commit();
            mostrarAlerta("Coche agregado correctamente.");

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error al agregar coche: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ✅ Actualizar coche (vehiculo + coche)
    public void actualizarCoche(Coche c) {
        String sqlVehiculo = "UPDATE vehiculo SET marca = ?, modelo = ?, año = ?, kilometraje = ?, usuario_id = ? WHERE id = ?";
        String sqlCoche = "UPDATE coche SET carroceria = ? WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo)) {
                stmtVeh.setString(1, c.getMarca());
                stmtVeh.setString(2, c.getModelo());
                stmtVeh.setInt(3, c.getAño());
                stmtVeh.setInt(4, c.getKilometraje());
                stmtVeh.setInt(5, c.getUsuarioId());
                stmtVeh.setInt(6, c.getId());
                stmtVeh.executeUpdate();
            }

            try (PreparedStatement stmtCoche = connection.prepareStatement(sqlCoche)) {
                stmtCoche.setString(1, c.getCarroceria());
                stmtCoche.setInt(2, c.getId());
                stmtCoche.executeUpdate();
            }

            connection.commit();
            mostrarAlerta("Coche actualizado correctamente.");

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error al actualizar coche: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ✅ Eliminar coche (de coche y vehiculo)
    public void eliminarCoche(Coche c, Runnable onSuccess) {
        String sqlCoche = "DELETE FROM coche WHERE id = ?";
        String sqlVehiculo = "DELETE FROM vehiculo WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtCoche = connection.prepareStatement(sqlCoche)) {
                stmtCoche.setInt(1, c.getId());
                stmtCoche.executeUpdate();
            }

            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo)) {
                stmtVeh.setInt(1, c.getId());
                stmtVeh.executeUpdate();
            }

            connection.commit();
            onSuccess.run();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            mostrarAlerta("Error al eliminar coche: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ✅ Alertas reutilizables
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mensaje");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ✅ Métodos FXML para botones (opcionalmente conectados desde FXML)
    @FXML
    private void agregarCoche() {
        mostrarFormularioCoche(null, this::cargarTablaCoche);
    }

    @FXML
    private void editarCoche() {
        Coche seleccionado = tableCoche.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un coche para editar.");
            return;
        }
        mostrarFormularioCoche(seleccionado, this::cargarTablaCoche);
    }

    @FXML
    private void eliminarCoche() {
        Coche seleccionado = tableCoche.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione un coche para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro que desea eliminar el coche?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                eliminarCoche(seleccionado, this::cargarTablaCoche);
            }
        });
    }
    public ObservableList<Coche> buscarCochesPorUsuario(int usuarioId) {
    ObservableList<Coche> lista = FXCollections.observableArrayList();
    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, c.carroceria
        FROM vehiculo v
        JOIN coche c ON v.id = c.id
        WHERE v.usuario_id = ?
    """;
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();
        StringBuilder info = new StringBuilder();
        while (rs.next()) {
            Coche c = new Coche(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id"),
                rs.getString("carroceria")
            );
            lista.add(c);
            info.append("ID: ").append(c.getId()).append("\n")
                .append("Marca: ").append(c.getMarca()).append("\n")
                .append("Modelo: ").append(c.getModelo()).append("\n")
                .append("Carrocería: ").append(c.getCarroceria()).append("\n")
                .append("-----------------------------\n");
        }
        if (!lista.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Coches del Usuario ID " + usuarioId);
            alert.setHeaderText("Se encontraron " + lista.size() + " coche(s):");
            alert.setContentText(info.toString());
            alert.getDialogPane().setPrefWidth(400);
            alert.showAndWait();
            return lista;
        } else {
            mostrarAlerta("No se encontraron coches para el usuario ID: " + usuarioId);
            return null;
        }
    } catch (SQLException e) {
        mostrarAlerta("Error al buscar coches: " + e.getMessage());
        return null;
    }
}
public ObservableList<Coche> buscarCochesPorCarroceria(String tipo) {
    ObservableList<Coche> lista = FXCollections.observableArrayList();

    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id,
               c.carroceria,
               u.nombre AS usuario_nombre, u.email AS usuario_email
        FROM vehiculo v
        JOIN coche c ON v.id = c.id
        JOIN usuario u ON v.usuario_id = u.id
        WHERE c.carroceria LIKE ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, "%" + tipo + "%");
        ResultSet rs = stmt.executeQuery();

        StringBuilder info = new StringBuilder();

        while (rs.next()) {
            Coche c = new Coche(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id"),
                rs.getString("carroceria")
            );
            lista.add(c);

            info.append("🚗 Coche ID: ").append(c.getId()).append("\n")
                .append("Marca: ").append(c.getMarca()).append("\n")
                .append("Modelo: ").append(c.getModelo()).append("\n")
                .append("Año: ").append(c.getAño()).append("\n")
                .append("Kilometraje: ").append(c.getKilometraje()).append(" km\n")
                .append("Carrocería: ").append(c.getCarroceria()).append("\n\n")
                .append("👤 Usuario:\n")
                .append("ID: ").append(c.getUsuarioId()).append("\n")
                .append("Nombre: ").append(rs.getString("usuario_nombre")).append("\n")
                .append("Email: ").append(rs.getString("usuario_email")).append("\n");

            // Buscar anuncios del coche
            String sqlAnuncio = "SELECT precio, descripcion FROM anuncio WHERE vehiculo_id = ?";
            try (PreparedStatement stmtAnuncio = connection.prepareStatement(sqlAnuncio)) {
                stmtAnuncio.setInt(1, c.getId());
                ResultSet rsAnuncio = stmtAnuncio.executeQuery();
                info.append("\n📢 Anuncios:\n");
                boolean tieneAnuncios = false;
                while (rsAnuncio.next()) {
                    tieneAnuncios = true;
                    info.append("- Precio: €").append(rsAnuncio.getBigDecimal("precio")).append("\n")
                        .append("  Descripción: ").append(rsAnuncio.getString("descripcion")).append("\n");
                }
                if (!tieneAnuncios) {
                    info.append("  Sin anuncios disponibles.\n");
                }
            }

            info.append("\n---------------------------\n");
        }

        if (!lista.isEmpty()) {
            // ✅ Dialog personalizado con scroll
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Coches con carrocería similar a: " + tipo);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            TextArea textArea = new TextArea(info.toString());
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setPrefWidth(500);
            textArea.setPrefHeight(400);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);

            dialog.getDialogPane().setContent(scrollPane);
            dialog.showAndWait();
        } else {
            mostrarAlerta("No se encontraron coches con carrocería: " + tipo);
        }

    } catch (SQLException e) {
        mostrarAlerta("Error al buscar coches por carrocería: " + e.getMessage());
    }

    return lista;
}


public void mostrarAnunciosDeCoche(int cocheId) {
    Alert info = new Alert(Alert.AlertType.INFORMATION);
    info.setTitle("Anuncios del Coche");
    info.setHeaderText("Anuncios para el Coche ID " + cocheId);

    StringBuilder contenido = new StringBuilder();
    String sql = "SELECT precio, descripcion FROM anuncio WHERE vehiculo_id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, cocheId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            contenido.append("- Precio: $").append(rs.getBigDecimal("precio")).append("\n")
                     .append("  Descripción: ").append(rs.getString("descripcion")).append("\n\n");
        }
        if (contenido.length() == 0) contenido.append("Sin anuncios.");
    } catch (SQLException e) {
        contenido.append("Error: ").append(e.getMessage());
    }

    info.setContentText(contenido.toString());
    info.getDialogPane().setPrefWidth(450);
    info.showAndWait();
}
public void mostrarInformacionCochePorId(int cocheId) {
    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id,
               c.carroceria, u.nombre, u.email
        FROM vehiculo v
        JOIN coche c ON v.id = c.id
        JOIN usuario u ON v.usuario_id = u.id
        WHERE v.id = ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, cocheId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            StringBuilder info = new StringBuilder();
            info.append("🚗 Coche ID: ").append(cocheId).append("\n")
                .append("Marca: ").append(rs.getString("marca")).append("\n")
                .append("Modelo: ").append(rs.getString("modelo")).append("\n")
                .append("Año: ").append(rs.getInt("año")).append("\n")
                .append("Kilometraje: ").append(rs.getInt("kilometraje")).append(" km\n")
                .append("Carrocería: ").append(rs.getString("carroceria")).append("\n\n")
                .append("👤 Propietario:\n")
                .append("ID: ").append(rs.getInt("usuario_id")).append("\n")
                .append("Nombre: ").append(rs.getString("nombre")).append("\n")
                .append("Email: ").append(rs.getString("email")).append("\n");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalles del Coche");
            alert.setHeaderText(null);
            alert.setContentText(info.toString());
            alert.getDialogPane().setPrefWidth(450);
            alert.showAndWait();
        } else {
            mostrarAlerta("No se encontró información para el coche ID: " + cocheId);
        }
    } catch (SQLException e) {
        mostrarAlerta("Error al obtener detalles del coche: " + e.getMessage());
    }
}

}

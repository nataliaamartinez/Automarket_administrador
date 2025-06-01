package com.example.Controlador;

import java.sql.*;
import com.example.Modelo.Moto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, m.cilindrada
        FROM moto m
        JOIN vehiculo v ON m.id = v.id
    """;

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            Moto moto = new Moto(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id"),
                rs.getInt("cilindrada")
            );
            lista.add(moto);
        }

        if (tableMoto == null) {
            System.err.println("❌ tableMoto es null. Verifica @FXML y fx:id.");
        } else {
            tableMoto.setItems(lista);
        }

    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al cargar motos: " + e.getMessage());
    }
}


    public void mostrarFormularioMoto(Moto moto) {
        Dialog<Moto> dialog = new Dialog<>();
        dialog.setTitle(moto == null ? "Agregar Moto" : "Editar Moto");

        TextField marcaField = new TextField();
        TextField modeloField = new TextField();
        TextField añoField = new TextField();
        TextField kilometrajeField = new TextField();
        TextField usuarioIdField = new TextField();
        TextField cilindradaField = new TextField();

        if (moto != null) {
            marcaField.setText(moto.getMarca());
            modeloField.setText(moto.getModelo());
            añoField.setText(String.valueOf(moto.getAño()));
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
        grid.add(añoField, 1, 2);
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
                try {
                    return new Moto(
                        moto != null ? moto.getId() : 0,
                        marcaField.getText().trim(),
                        modeloField.getText().trim(),
                        Integer.parseInt(añoField.getText().trim()),
                        Integer.parseInt(kilometrajeField.getText().trim()),
                        Integer.parseInt(usuarioIdField.getText().trim()),
                        Integer.parseInt(cilindradaField.getText().trim())
                    );
                } catch (Exception e) {
                    mostrarAlerta("Todos los campos deben ser válidos y no vacíos.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(m -> {
            if (moto == null) agregarMoto(m);
            else actualizarMoto(m);
            cargarTablaMoto();
        });
    }

    public void agregarMoto(Moto m) {
        String sqlVehiculo = "INSERT INTO vehiculo (marca, modelo, año, kilometraje, usuario_id) VALUES (?, ?, ?, ?, ?)";
        String sqlMoto = "INSERT INTO moto (id, cilindrada) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            int idGenerado;
            try (PreparedStatement stmtVeh = connection.prepareStatement(sqlVehiculo, Statement.RETURN_GENERATED_KEYS)) {
                stmtVeh.setString(1, m.getMarca());
                stmtVeh.setString(2, m.getModelo());
                stmtVeh.setInt(3, m.getAño());
                stmtVeh.setInt(4, m.getKilometraje());
                stmtVeh.setInt(5, m.getUsuarioId());
                stmtVeh.executeUpdate();

                ResultSet rsKeys = stmtVeh.getGeneratedKeys();
                if (rsKeys.next()) idGenerado = rsKeys.getInt(1);
                else throw new SQLException("No se pudo obtener el ID generado.");
            }

            try (PreparedStatement stmtMoto = connection.prepareStatement(sqlMoto)) {
                stmtMoto.setInt(1, idGenerado);
                stmtMoto.setInt(2, m.getCilindrada());
                stmtMoto.executeUpdate();
            }

            connection.commit();
            mostrarAlerta("Moto agregada correctamente.");
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Error al agregar moto: " + e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    public void actualizarMoto(Moto m) {
        String sqlVehiculo = "UPDATE vehiculo SET marca = ?, modelo = ?, año = ?, kilometraje = ?, usuario_id = ? WHERE id = ?";
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
            mostrarAlerta("Moto actualizada correctamente.");
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Error al actualizar moto: " + e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

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
            cargarTablaMoto();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Error al eliminar moto: " + e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

  public ObservableList<Moto> buscarMotosPorUsuario(int usuarioId) {
    ObservableList<Moto> lista = FXCollections.observableArrayList();

    String sql = """
        SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, m.cilindrada
        FROM vehiculo v
        JOIN moto m ON v.id = m.id
        WHERE v.usuario_id = ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();

        StringBuilder info = new StringBuilder();

        while (rs.next()) {
            Moto moto = new Moto(
                rs.getInt("id"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("año"),
                rs.getInt("kilometraje"),
                rs.getInt("usuario_id"),
                rs.getInt("cilindrada")
            );
            lista.add(moto);

            info.append("ID: ").append(moto.getId()).append("\n")
                .append("Marca: ").append(moto.getMarca()).append("\n")
                .append("Modelo: ").append(moto.getModelo()).append("\n")
                .append("Año: ").append(moto.getAño()).append("\n")
                .append("Kilometraje: ").append(moto.getKilometraje()).append(" km\n")
                .append("Usuario ID: ").append(moto.getUsuarioId()).append("\n")
                .append("Cilindrada: ").append(moto.getCilindrada()).append(" cc\n")
                .append("---------------------------\n");
        }

        if (!lista.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Motos del Usuario ID " + usuarioId);
            alert.setHeaderText("Se encontraron " + lista.size() + " moto(s):");
            alert.setContentText(info.toString());
            alert.getDialogPane().setPrefWidth(450); // para que no se corte el texto
            alert.showAndWait();
            return lista;
        } else {
            mostrarAlerta("No se encontraron motos para el usuario ID: " + usuarioId);
            return null;
        }

    } catch (SQLException e) {
        mostrarAlerta("Error al buscar motos por usuario: " + e.getMessage());
        return null;
    }
}
    public ObservableList<Moto> buscarMotosPorCilindrada(int cilindradaMin) {
        ObservableList<Moto> lista = FXCollections.observableArrayList();
        String sql = """
            SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id, m.cilindrada
            FROM vehiculo v
            JOIN moto m ON v.id = m.id
            WHERE m.cilindrada >= ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cilindradaMin);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Moto moto = new Moto(
                    rs.getInt("id"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("año"),
                    rs.getInt("kilometraje"),
                    rs.getInt("usuario_id"),
                    rs.getInt("cilindrada")
                );
                lista.add(moto);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al buscar motos por cilindrada: " + e.getMessage());
        }
        return lista;
    }

    public void mostrarAnunciosDeMoto(int motoId) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Anuncios");
        info.setHeaderText("Anuncios para Moto ID " + motoId);

        StringBuilder contenido = new StringBuilder();
        String sql = "SELECT precio, descripcion FROM anuncio WHERE vehiculo_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, motoId);
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

    public void mostrarInformacionMotoPorId(int motoId) {
        String sql = """
            SELECT v.id, v.marca, v.modelo, v.año, v.kilometraje, v.usuario_id,
                   m.cilindrada, u.nombre, u.email
            FROM vehiculo v
            JOIN moto m ON v.id = m.id
            JOIN usuario u ON v.usuario_id = u.id
            WHERE m.id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, motoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                StringBuilder info = new StringBuilder();
                info.append("📌 Moto ID: ").append(motoId).append("\n")
                    .append("Marca: ").append(rs.getString("marca")).append("\n")
                    .append("Modelo: ").append(rs.getString("modelo")).append("\n")
                    .append("Año: ").append(rs.getInt("año")).append("\n")
                    .append("Kilometraje: ").append(rs.getInt("kilometraje")).append(" km\n")
                    .append("Cilindrada: ").append(rs.getInt("cilindrada")).append(" cc\n\n")
                    .append("👤 Usuario:\n")
                    .append("ID: ").append(rs.getInt("usuario_id")).append("\n")
                    .append("Nombre: ").append(rs.getString("nombre")).append("\n")
                    .append("Email: ").append(rs.getString("email")).append("\n");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Detalles de la Moto");
                alert.setHeaderText(null);
                alert.setContentText(info.toString());
                alert.getDialogPane().setPrefWidth(450);
                alert.showAndWait();
            } else {
                mostrarAlerta("No se encontró información para la moto ID: " + motoId);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al obtener información: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML private void agregarMoto() { mostrarFormularioMoto(null); }
    @FXML private void editarMoto() {
        Moto seleccionado = tableMoto.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccione una moto para editar.");
        } else {
            mostrarFormularioMoto(seleccionado);
        }
    }
    @FXML private void eliminarMoto() {
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

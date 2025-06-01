package com.example.Controlador;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.Modelo.Anuncio;
import com.example.Modelo.Usuario;
import com.example.Modelo.Vehiculo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class ControllerUsuario {

    private final Connection connection;
     private final TableView<Usuario> tableUsuario;

    public ControllerUsuario(Connection connection, TableView<Usuario> tableUsuario) {
        this.connection = connection;
        this.tableUsuario = tableUsuario;
    }

    public void mostrarFormularioUsuario(Usuario usuario, Runnable onSuccess) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle(usuario == null ? "Agregar Usuario" : "Editar Usuario");

        TextField nombreField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        if (usuario != null) {
            nombreField.setText(usuario.getNombre());
            emailField.setText(usuario.getEmail());
            passwordField.setText(usuario.getContrasenia());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(passwordField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Usuario(
                    usuario != null ? usuario.getId() : 0,
                    nombreField.getText(),
                    emailField.getText(),
                    passwordField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            if (usuario == null) {
                agregarUsuario(u);
            } else {
                actualizarUsuario(u);
            }
            onSuccess.run();
        });
    }

    public void agregarUsuario(Usuario u) {
        String sql = "INSERT INTO usuario (nombre, email, contrasenia) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getEmail());
            stmt.setString(3, u.getContrasenia());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al agregar usuario: " + e.getMessage());
        }
    }

    public void actualizarUsuario(Usuario u) {
        String sql = "UPDATE usuario SET nombre = ?, email = ?, contrasenia = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, u.getNombre());
            stmt.setString(2, u.getEmail());
            stmt.setString(3, u.getContrasenia());
            stmt.setInt(4, u.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al actualizar usuario: " + e.getMessage());
        }
    }

    public void eliminarUsuario(Usuario u, Runnable onSuccess) {
    if (u == null || u.getId() == null) {
        mostrarAlerta("El usuario a eliminar no es válido.");
        return;
    }

    // Alerta de confirmación
    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
    confirmacion.setTitle("Confirmar eliminación");
    confirmacion.setHeaderText("¿Estás seguro que quieres eliminar este usuario?");
    confirmacion.setContentText("Se eliminarán también todos los anuncios y vehículos asociados a este usuario.");

    Optional<ButtonType> resultado = confirmacion.showAndWait();
    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
        // Si el usuario confirma, se procede a eliminar
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, u.getId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                onSuccess.run();
            } else {
                mostrarAlerta("No se encontró el usuario con ID: " + u.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar usuario: " + e.getMessage());
        }
    } else {
        mostrarAlerta("Eliminación cancelada.");
    }
}

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

      public void cargarTablaUsuario() {
        ObservableList<Usuario> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM usuario";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("contrasenia")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar usuarios: " + e.getMessage());
        }
        tableUsuario.setItems(lista);
    }
   public List<Anuncio> buscarAnunciosPorUsuarioId(int usuarioId) {
    List<Anuncio> resultados = new ArrayList<>();
    String sql = "SELECT * FROM anuncio WHERE vendedor_id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, usuarioId);
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

        // Mostrar cantidad encontrada
        if (!resultados.isEmpty()) {
            mostrarAlerta("El usuario con ID " + usuarioId + " tiene " + resultados.size() + " anuncio(s).");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar anuncios del usuario: " + e.getMessage());
    }
    return resultados;

}

public List<Anuncio> buscarFavoritosPorUsuarioId(int usuarioId) {
    List<Anuncio> favoritos = new ArrayList<>();
    String sql = """
        SELECT a.*, u.nombre FROM favorito f
        JOIN anuncio a ON f.anuncio_id = a.id
        JOIN usuario u ON f.comprador_id = u.id
        WHERE f.comprador_id = ?
    """;
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();

        String nombreUsuario = null;

        while (rs.next()) {
            if (nombreUsuario == null) {
                nombreUsuario = rs.getString("nombre");
            }
            favoritos.add(new Anuncio(
                rs.getInt("id"),
                rs.getInt("vehiculo_id"),
                rs.getDouble("precio"),
                rs.getString("descripcion"),
                rs.getInt("vendedor_id")
            ));
        }

        if (nombreUsuario != null && !favoritos.isEmpty()) {
            mostrarAlerta("El usuario \"" + nombreUsuario + "\" (ID " + usuarioId + ") tiene " + favoritos.size() + " favorito(s).");
        } else {
            mostrarAlerta("El usuario con ID " + usuarioId + " no tiene favoritos o no existe.");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al obtener favoritos del usuario: " + e.getMessage());
    }

    return favoritos;
}public void buscarResumenPorNombreUsuario(String nombre) {
    String usuarioSql = "SELECT * FROM usuario WHERE nombre LIKE ?";
    try (PreparedStatement stmt = connection.prepareStatement(usuarioSql)) {
        stmt.setString(1, "%" + nombre + "%");
        ResultSet rsUsuario = stmt.executeQuery();

        if (!rsUsuario.next()) {
            mostrarAlerta("No se encontró ningún usuario con nombre parecido a: " + nombre);
            return;
        }

        int id = rsUsuario.getInt("id");
        String nombreReal = rsUsuario.getString("nombre");
        String email = rsUsuario.getString("email");

        // Vehículos
        List<Vehiculo> vehiculos = new ArrayList<>();
        String vehiculoSql = "SELECT * FROM vehiculo WHERE usuario_id = ?";
        try (PreparedStatement stmtVeh = connection.prepareStatement(vehiculoSql)) {
            stmtVeh.setInt(1, id);
            ResultSet rsVeh = stmtVeh.executeQuery();
            while (rsVeh.next()) {
                vehiculos.add(new Vehiculo(
                    rsVeh.getInt("id"),
                    rsVeh.getString("marca"),
                    rsVeh.getString("modelo"),
                    rsVeh.getInt("año"),
                    rsVeh.getInt("kilometraje"),
                    rsVeh.getInt("usuario_id")
                ));
            }
        }

        // Anuncios
        List<Anuncio> anuncios = new ArrayList<>();
        String anuncioSql = "SELECT * FROM anuncio WHERE vendedor_id = ?";
        try (PreparedStatement stmtAn = connection.prepareStatement(anuncioSql)) {
            stmtAn.setInt(1, id);
            ResultSet rsAn = stmtAn.executeQuery();
            while (rsAn.next()) {
                anuncios.add(new Anuncio(
                    rsAn.getInt("id"),
                    rsAn.getInt("vehiculo_id"),
                    rsAn.getDouble("precio"),
                    rsAn.getString("descripcion"),
                    rsAn.getInt("vendedor_id")
                ));
            }
        }

        // Favoritos
        List<Anuncio> favoritos = new ArrayList<>();
        String favoritoSql = "SELECT a.* FROM favorito f JOIN anuncio a ON f.anuncio_id = a.id WHERE f.comprador_id = ?";
        try (PreparedStatement stmtFav = connection.prepareStatement(favoritoSql)) {
            stmtFav.setInt(1, id);
            ResultSet rsFav = stmtFav.executeQuery();
            while (rsFav.next()) {
                favoritos.add(new Anuncio(
                    rsFav.getInt("id"),
                    rsFav.getInt("vehiculo_id"),
                    rsFav.getDouble("precio"),
                    rsFav.getString("descripcion"),
                    rsFav.getInt("vendedor_id")
                ));
            }
        }

        // Mostrar resultados
        StringBuilder sb = new StringBuilder();
        sb.append("📄 Información del usuario:\n")
          .append("ID: ").append(id).append("\n")
          .append("Nombre: ").append(nombreReal).append("\n")
          .append("Email: ").append(email).append("\n\n");

        sb.append("🚗 Vehículos registrados: ").append(vehiculos.size()).append("\n");
        for (Vehiculo v : vehiculos) {
            sb.append("  - ").append(v.getMarca()).append(" ").append(v.getModelo()).append(" (").append(v.getAño()).append(")\n");
        }

        sb.append("\n📢 Anuncios publicados: ").append(anuncios.size()).append("\n");
        for (Anuncio a : anuncios) {
            sb.append("  - ID: ").append(a.getId()).append(" | Descripción: ").append(a.getDescripcion()).append("\n");
        }

        sb.append("\n⭐ Favoritos guardados: ").append(favoritos.size()).append("\n");
        for (Anuncio f : favoritos) {
            sb.append("  - ID: ").append(f.getId()).append(" | Descripción: ").append(f.getDescripcion()).append("\n");
        }

        Alert resumen = new Alert(Alert.AlertType.INFORMATION);
        resumen.setTitle("Resumen del Usuario");
        resumen.setHeaderText("Resumen completo de " + nombreReal);
        resumen.setContentText(sb.toString());
        resumen.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        resumen.showAndWait();

    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar resumen del usuario: " + e.getMessage());
    }
}
public Usuario buscarUsuarioPorId(int id) {
    String sql = "SELECT * FROM usuario WHERE id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getString("contrasenia")
            );
        } else {
            mostrarAlerta("No se encontró ningún usuario con ID: " + id);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar el usuario: " + e.getMessage());
    }
    return null;
}
}


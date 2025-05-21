package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

import com.example.Modelo.*;

public class PantallaPrincipalController {

    @FXML private Button cerrarSesionButton;
    @FXML private Button guardarCambiosButton;
    @FXML private Button exportarTXTButton;
    @FXML private TextField buscarUsuarioField;

    @FXML private TableView<Anuncio> tableAnuncio;
    @FXML private TableView<Archivo> tableArchivo;
    @FXML private TableView<Coche> tableCoche;
    @FXML private TableView<Favorito> tableFavorito;
    @FXML private TableView<Furgoneta> tableFurgoneta;
    @FXML private TableView<Moto> tableMoto;
    @FXML private TableView<Usuario> tableUsuario;
    @FXML private TableView<Vehiculo> tableVehiculo;

    @FXML private TabPane tabPane;
    @FXML private HBox actionButtonsBox;

    private Connection connection;

    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/automarket_", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo conectar a la base de datos.");
            return;
        }

        configurarColumnas();
        cargarDatos();

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            actualizarBotonesPorPestana(newTab);
        });

        actualizarBotonesPorPestana(tabPane.getSelectionModel().getSelectedItem());

        guardarCambiosButton.setOnAction(e -> guardarCambios());
        exportarTXTButton.setOnAction(e -> exportarATXT());
    }

    private void guardarCambios() {
        // Aquí se implementaría la lógica para guardar los cambios reales
        mostrarAlertaInfo("Todos los cambios han sido guardados.");
    }

    private void exportarATXT() {
        String outputDir = "C:/AutomarketTXTs";
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        try {
            exportarTablaTXT("Usuarios", tableUsuario.getItems(), outputDir + "/usuarios.txt");
            exportarTablaTXT("Vehículos", tableVehiculo.getItems(), outputDir + "/vehiculos.txt");
            exportarTablaTXT("Anuncios", tableAnuncio.getItems(), outputDir + "/anuncios.txt");
            exportarTablaTXT("Archivos", tableArchivo.getItems(), outputDir + "/archivos.txt");
            exportarTablaTXT("Coches", tableCoche.getItems(), outputDir + "/coches.txt");
            exportarTablaTXT("Favoritos", tableFavorito.getItems(), outputDir + "/favoritos.txt");
            exportarTablaTXT("Furgonetas", tableFurgoneta.getItems(), outputDir + "/furgonetas.txt");
            exportarTablaTXT("Motos", tableMoto.getItems(), outputDir + "/motos.txt");

            mostrarAlertaInfo("Todos los datos han sido exportados a TXT en: " + outputDir);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error al exportar a TXT: " + e.getMessage());
        }
    }

    private <T> void exportarTablaTXT(String titulo, ObservableList<T> datos, String rutaSalida) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaSalida))) {
            writer.write(titulo);
            writer.newLine();
            writer.write("------------------------");
            writer.newLine();

            for (T item : datos) {
                writer.write(item.toString());
                writer.newLine();
            }
        }
    }

    private void actualizarBotonesPorPestana(Tab tabActiva) {
        actionButtonsBox.getChildren().clear();

        if (tabActiva == null) return;

        String nombre = tabActiva.getText();

        switch (nombre) {
            case "Usuario":
                Button agregarBtn = new Button("Agregar Usuario");
                Button editarBtn = new Button("Editar Usuario");
                Button eliminarBtn = new Button("Eliminar Usuario");

                agregarBtn.setOnAction(e -> mostrarFormularioUsuario(null));
                editarBtn.setOnAction(e -> {
                    Usuario seleccionado = tableUsuario.getSelectionModel().getSelectedItem();
                    if (seleccionado != null) {
                        mostrarFormularioUsuario(seleccionado);
                    } else {
                        mostrarAlerta("Selecciona un usuario para editar.");
                    }
                });
                eliminarBtn.setOnAction(e -> {
                    Usuario seleccionado = tableUsuario.getSelectionModel().getSelectedItem();
                    if (seleccionado != null) {
                        eliminarUsuario(seleccionado);
                    } else {
                        mostrarAlerta("Selecciona un usuario para eliminar.");
                    }
                });

                actionButtonsBox.getChildren().addAll(agregarBtn, editarBtn, eliminarBtn);
                break;

            // Aquí puedes agregar más casos para otras pestañas si lo deseas

            default:
                actionButtonsBox.getChildren().add(new Label("Sin acciones disponibles."));
        }
    }

    private void mostrarFormularioUsuario(Usuario usuario) {
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
            cargarTablaUsuario();
        });
    }

    private void agregarUsuario(Usuario u) {
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

    private void actualizarUsuario(Usuario u) {
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

    private void eliminarUsuario(Usuario u) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, u.getId());
            stmt.executeUpdate();
            cargarTablaUsuario();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al eliminar usuario: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void configurarColumnas() {
        configurarColumnasAnuncio();
        configurarColumnasArchivo();
        configurarColumnasCoche();
        configurarColumnasFavorito();
        configurarColumnasFurgoneta();
        configurarColumnasMoto();
        configurarColumnasUsuario();
        configurarColumnasVehiculo();
    }

    private void configurarColumnasAnuncio() {
        tableAnuncio.getColumns().clear();
        tableAnuncio.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Vehiculo ID", "vehiculoId", 100),
            crearColumna("Precio", "precio", 100),
            crearColumna("Descripción", "descripcion", 200),
            crearColumna("Vendedor ID", "vendedorId", 100),
            crearColumna("Archivo ID", "archivoId", 100)
        );
    }

    private void configurarColumnasArchivo() {
        tableArchivo.getColumns().clear();
        tableArchivo.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Archivo Path", "archivoPath", 250)
        );
    }

    private void configurarColumnasCoche() {
        tableCoche.getColumns().clear();
        tableCoche.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Carrocería", "carroceria", 150)
        );
    }

    private void configurarColumnasFavorito() {
        tableFavorito.getColumns().clear();
        tableFavorito.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Anuncio ID", "anuncioId", 100),
            crearColumna("Comprador ID", "compradorId", 100)
        );
    }

    private void configurarColumnasFurgoneta() {
        tableFurgoneta.getColumns().clear();
        tableFurgoneta.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Capacidad Carga", "capacidadCarga", 150)
        );
    }

    private void configurarColumnasMoto() {
        tableMoto.getColumns().clear();
        tableMoto.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Cilindrada", "cilindrada", 100)
        );
    }

    private void configurarColumnasUsuario() {
        tableUsuario.getColumns().clear();
        tableUsuario.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Nombre", "nombre", 150),
            crearColumna("Email", "email", 200),
            crearColumna("Password", "contrasenia", 150)
        );
    }

    private void configurarColumnasVehiculo() {
        tableVehiculo.getColumns().clear();
        tableVehiculo.getColumns().addAll(
            crearColumna("ID", "id", 50),
            crearColumna("Marca", "marca", 100),
            crearColumna("Modelo", "modelo", 100),
            crearColumna("Año", "año", 60),
            crearColumna("Kilometraje", "kilometraje", 100),
            crearColumna("Usuario ID", "usuarioId", 100)
        );
    }

    private <T> TableColumn<T, ?> crearColumna(String titulo, String propiedad, int ancho) {
        TableColumn<T, Object> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        columna.setPrefWidth(ancho);
        return columna;
    }

    private void cargarDatos() {
        cargarTablaAnuncio();
        cargarTablaArchivo();
        cargarTablaCoche();
        cargarTablaFavorito();
        cargarTablaFurgoneta();
        cargarTablaMoto();
        cargarTablaUsuario();
        cargarTablaVehiculo();
    }

    private void cargarTablaAnuncio() {
        ObservableList<Anuncio> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM anuncio";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Integer archivoId = rs.getObject("archivo_id") != null ? rs.getInt("archivo_id") : null;
                lista.add(new Anuncio(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion"),
                    rs.getInt("vendedor_id"),
                    archivoId
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar anuncios: " + e.getMessage());
        }
        tableAnuncio.setItems(lista);
    }

    private void cargarTablaArchivo() {
        ObservableList<Archivo> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM archivo";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Archivo(rs.getInt("id"), rs.getString("archivo_path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar archivos: " + e.getMessage());
        }
        tableArchivo.setItems(lista);
    }

    private void cargarTablaCoche() {
        ObservableList<Coche> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM coche";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Coche(rs.getInt("id"), rs.getString("carroceria")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar coches: " + e.getMessage());
        }
        tableCoche.setItems(lista);
    }

    private void cargarTablaFavorito() {
        ObservableList<Favorito> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM favorito";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Favorito(
                    rs.getInt("id"),
                    rs.getInt("anuncio_id"),
                    rs.getInt("comprador_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar favoritos: " + e.getMessage());
        }
        tableFavorito.setItems(lista);
    }

    private void cargarTablaFurgoneta() {
        ObservableList<Furgoneta> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM furgoneta";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Furgoneta(rs.getInt("id"), rs.getDouble("capacidadcarga")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar furgonetas: " + e.getMessage());
        }
        tableFurgoneta.setItems(lista);
    }

    private void cargarTablaMoto() {
        ObservableList<Moto> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM moto";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Moto(rs.getInt("id"), rs.getInt("cilindrada")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar motos: " + e.getMessage());
        }
        tableMoto.setItems(lista);
    }

    private void cargarTablaUsuario() {
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

    private void cargarTablaVehiculo() {
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
    @FXML
    private void exportarDatos(ActionEvent event) {
        // Código para exportar datos
    }

    @FXML
    private void cerrarSesion() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) cerrarSesionButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo cargar la pantalla de login.");
        }
    }
}

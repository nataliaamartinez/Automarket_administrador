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

import com.example.Controlador.Alertas;
import com.example.Controlador.Constantes;
import com.example.Controlador.ControllerAnuncio;
import com.example.Controlador.ControllerArchivo;
import com.example.Controlador.ControllerCoche;
import com.example.Controlador.ControllerFavoritos;
import com.example.Controlador.ControllerFurgoneta;
import com.example.Controlador.ControllerMoto;
import com.example.Controlador.ControllerUsuario;
import com.example.Controlador.ControllerVehiculo;
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
   private ControllerAnuncio controllerAnuncio;
private ControllerArchivo controllerArchivo;
private ControllerCoche controllerCoche;
private ControllerFavoritos controllerFavoritos;
private ControllerFurgoneta controllerFurgoneta;
private ControllerMoto controllerMoto;
private ControllerUsuario controllerUsuario;
private ControllerVehiculo controllerVehiculo;
    
    private  Alertas alertas = new Alertas();




    @FXML
public void initialize() {
    try {
        // Usamos los valores definidos en Constantes
        connection = DriverManager.getConnection(Constantes.DB_URL, Constantes.DB_USER, Constantes.DB_PASSWORD);
    } catch (SQLException e) {
        e.printStackTrace();
        Alertas alertas = new Alertas();
        alertas.mostrarAlerta("No se pudo conectar a la base de datos.");
        return;
    }

    //  Inicializamos los controladores después de tener la conexión activa
    controllerMoto = new ControllerMoto(connection, tableMoto);
    controllerMoto.cargarTablaMoto();

    controllerFurgoneta = new ControllerFurgoneta(connection, tableFurgoneta);
    controllerFurgoneta.cargarTablaFurgoneta();

    controllerFavoritos = new ControllerFavoritos(connection, tableFavorito);
    controllerFavoritos.cargarTablaFavorito();

    controllerCoche = new ControllerCoche(connection, tableCoche);
    controllerCoche.cargarTablaCoche();

    controllerAnuncio = new ControllerAnuncio(connection, tableAnuncio);
    controllerAnuncio.cargarTablaAnuncio();

    controllerArchivo = new ControllerArchivo(connection, tableArchivo);
    controllerArchivo.cargarTablaArchivo();

    controllerUsuario = new ControllerUsuario(connection, tableUsuario);
    controllerUsuario.cargarTablaUsuario();

    controllerVehiculo = new ControllerVehiculo(connection, tableVehiculo);
    controllerVehiculo.cargarTablaVehiculo();

    // 🛠 Resto de configuraciones de la interfaz
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
        //mostrarAlertaInfo("Todos los cambios han sido guardados.");
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

            alertas.mostrarAlertaInfo("Todos los datos han sido exportados a TXT en: " + outputDir);
        } catch (IOException e) {
            e.printStackTrace();
            alertas.mostrarAlerta("Error al exportar a TXT: " + e.getMessage());
            return;
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
    case "Usuario": {
        Button agregarUsuarioBtn = new Button("Agregar Usuario");
        Button editarUsuarioBtn = new Button("Editar Usuario");
        Button eliminarUsuarioBtn = new Button("Eliminar Usuario");

        agregarUsuarioBtn.setOnAction(e -> 
            controllerUsuario.mostrarFormularioUsuario(null, () -> controllerUsuario.cargarTablaUsuario())
        );

        editarUsuarioBtn.setOnAction(e -> {
            Usuario seleccionado = tableUsuario.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                controllerUsuario.mostrarFormularioUsuario(seleccionado, () -> controllerUsuario.cargarTablaUsuario());
            } else {
                alertas.mostrarAlerta("Selecciona un usuario para editar.");
            }
        });

        eliminarUsuarioBtn.setOnAction(e -> {
            Usuario seleccionado = tableUsuario.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                controllerUsuario.eliminarUsuario(seleccionado, () -> controllerUsuario.cargarTablaUsuario());
            } else {
                alertas.mostrarAlerta("Selecciona un usuario para eliminar.");
            }
        });

        actionButtonsBox.getChildren().addAll(agregarUsuarioBtn, editarUsuarioBtn, eliminarUsuarioBtn);
        break;
    }

    case "Vehiculo": {
        Button agregarVehiculoBtn = new Button("Agregar Vehículo");
        Button editarVehiculoBtn = new Button("Editar Vehículo");
        Button eliminarVehiculoBtn = new Button("Eliminar Vehículo");

        agregarVehiculoBtn.setOnAction(e -> 
            controllerVehiculo.mostrarFormularioVehiculo(null, () -> controllerVehiculo.cargarTablaVehiculo())
        );

        editarVehiculoBtn.setOnAction(e -> {
            Vehiculo seleccionado = tableVehiculo.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                controllerVehiculo.mostrarFormularioVehiculo(seleccionado, () -> controllerVehiculo.cargarTablaVehiculo());
            } else {
                alertas.mostrarAlerta("Selecciona un vehículo para editar.");
            }
        });

        eliminarVehiculoBtn.setOnAction(e -> {
            Vehiculo seleccionado = tableVehiculo.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                controllerVehiculo.eliminarVehiculo(seleccionado, () -> controllerVehiculo.cargarTablaVehiculo());
            } else {
                alertas.mostrarAlerta("Selecciona un vehículo para eliminar.");
            }
        });

        actionButtonsBox.getChildren().addAll(agregarVehiculoBtn, editarVehiculoBtn, eliminarVehiculoBtn);
        break;
    }

    case "Anuncio": {
        Button agregarAnuncioBtn = new Button("Agregar Anuncio");
        Button editarAnuncioBtn = new Button("Editar Anuncio");
        Button eliminarAnuncioBtn = new Button("Eliminar Anuncio");

        agregarAnuncioBtn.setOnAction(e -> 
            controllerAnuncio.mostrarFormularioAnuncio(null, () -> controllerAnuncio.cargarTablaAnuncio())
        );

        editarAnuncioBtn.setOnAction(e -> {
            Anuncio seleccionado = tableAnuncio.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                controllerAnuncio.mostrarFormularioAnuncio(seleccionado, () -> controllerAnuncio.cargarTablaAnuncio());
            } else {
                alertas.mostrarAlerta("Selecciona un anuncio para editar.");
            }
        });

        eliminarAnuncioBtn.setOnAction(e -> {
            Anuncio seleccionado = tableAnuncio.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                controllerAnuncio.eliminarAnuncio(seleccionado, () -> controllerAnuncio.cargarTablaAnuncio());
            } else {
                alertas.mostrarAlerta("Selecciona un anuncio para eliminar.");
            }
        });

        actionButtonsBox.getChildren().addAll(agregarAnuncioBtn, editarAnuncioBtn, eliminarAnuncioBtn);
        break;
    }

   case "Archivo": {
    Button agregarArchivoBtn = new Button("Agregar Archivo");
    Button editarArchivoBtn = new Button("Editar Archivo");
    Button eliminarArchivoBtn = new Button("Eliminar Archivo");

    agregarArchivoBtn.setOnAction(e -> 
        controllerArchivo.mostrarFormularioArchivo(null, () -> controllerArchivo.cargarTablaArchivo())
    );

    editarArchivoBtn.setOnAction(e -> {
        Archivo seleccionado = tableArchivo.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerArchivo.mostrarFormularioArchivo(seleccionado, () -> controllerArchivo.cargarTablaArchivo());
        } else {
            alertas.mostrarAlerta("Selecciona un archivo para editar.");
        }
    });

    eliminarArchivoBtn.setOnAction(e -> {
        Archivo seleccionado = tableArchivo.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerArchivo.eliminarArchivo(seleccionado, () -> controllerArchivo.cargarTablaArchivo());
        } else {
            alertas.mostrarAlerta("Selecciona un archivo para eliminar.");
        }
    });

    actionButtonsBox.getChildren().addAll(agregarArchivoBtn, editarArchivoBtn, eliminarArchivoBtn);
    break;
}
    case "Coche": {
    Button agregarCocheBtn = new Button("Agregar Coche");
    Button editarCocheBtn = new Button("Editar Coche");
    Button eliminarCocheBtn = new Button("Eliminar Coche");

    agregarCocheBtn.setOnAction(e -> 
        controllerCoche.mostrarFormularioCoche(null, () -> controllerCoche.cargarTablaCoche())
    );

    editarCocheBtn.setOnAction(e -> {
        Coche seleccionado = tableCoche.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerCoche.mostrarFormularioCoche(seleccionado, () -> controllerCoche.cargarTablaCoche());
        } else {
            alertas.mostrarAlerta("Selecciona un coche para editar.");
        }
    });

    eliminarCocheBtn.setOnAction(e -> {
        Coche seleccionado = tableCoche.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerCoche.eliminarCoche(seleccionado, () -> controllerCoche.cargarTablaCoche());
        } else {
            alertas.mostrarAlerta("Selecciona un coche para eliminar.");
        }
    });

    actionButtonsBox.getChildren().addAll(agregarCocheBtn, editarCocheBtn, eliminarCocheBtn);
    break;
}
case "Favorito": {
    Button agregarFavoritoBtn = new Button("Agregar Favorito");
    Button editarFavoritoBtn = new Button("Editar Favorito");
    Button eliminarFavoritoBtn = new Button("Eliminar Favorito");

    agregarFavoritoBtn.setOnAction(e -> 
        controllerFavoritos.mostrarFormularioFavorito(null, () -> controllerFavoritos.cargarTablaFavorito())
    );

    editarFavoritoBtn.setOnAction(e -> {
        Favorito seleccionado = tableFavorito.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerFavoritos.mostrarFormularioFavorito(seleccionado, () -> controllerFavoritos.cargarTablaFavorito());
        } else {
            alertas.mostrarAlerta("Selecciona un favorito para editar.");
        }
    });

    eliminarFavoritoBtn.setOnAction(e -> {
        Favorito seleccionado = tableFavorito.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerFavoritos.eliminarFavorito(seleccionado, () -> controllerFavoritos.cargarTablaFavorito());
        } else {
            alertas.mostrarAlerta("Selecciona un favorito para eliminar.");
        }
    });

    actionButtonsBox.getChildren().addAll(agregarFavoritoBtn, editarFavoritoBtn, eliminarFavoritoBtn);
    break;
}


  case "Furgoneta": {
    Button agregarFurgonetaBtn = new Button("Agregar Furgoneta");
    Button editarFurgonetaBtn = new Button("Editar Furgoneta");
    Button eliminarFurgonetaBtn = new Button("Eliminar Furgoneta");

    agregarFurgonetaBtn.setOnAction(e -> 
        controllerFurgoneta.mostrarFormularioFurgoneta(null)
    );

    editarFurgonetaBtn.setOnAction(e -> {
        Furgoneta seleccionado = tableFurgoneta.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerFurgoneta.mostrarFormularioFurgoneta(seleccionado);
        } else {
            alertas.mostrarAlerta("Selecciona una furgoneta para editar.");
        }
    });

    eliminarFurgonetaBtn.setOnAction(e -> {
        Furgoneta seleccionado = tableFurgoneta.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerFurgoneta.eliminarFurgoneta(seleccionado);
        } else {
            alertas.mostrarAlerta("Selecciona una furgoneta para eliminar.");
        }
    });

    actionButtonsBox.getChildren().addAll(agregarFurgonetaBtn, editarFurgonetaBtn, eliminarFurgonetaBtn);
    break;
}

    case "Moto": {
    Button agregarMotoBtn = new Button("Agregar Moto");
    Button editarMotoBtn = new Button("Editar Moto");
    Button eliminarMotoBtn = new Button("Eliminar Moto");

    agregarMotoBtn.setOnAction(e -> 
        controllerMoto.mostrarFormularioMoto(null)
    );

    editarMotoBtn.setOnAction(e -> {
        Moto seleccionado = tableMoto.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerMoto.mostrarFormularioMoto(seleccionado);
        } else {
            alertas.mostrarAlerta("Selecciona una moto para editar.");
        }
    });

    eliminarMotoBtn.setOnAction(e -> {
        Moto seleccionado = tableMoto.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            controllerMoto.eliminarMoto(seleccionado);
        } else {
            alertas.mostrarAlerta("Selecciona una moto para eliminar.");
        }
    });

    actionButtonsBox.getChildren().addAll(agregarMotoBtn, editarMotoBtn, eliminarMotoBtn);
    break;
}
    default:
        // No hay botones para otras pestañas
        break;
        }       


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
     controllerAnuncio.cargarTablaAnuncio();
    controllerArchivo.cargarTablaArchivo();
    controllerCoche.cargarTablaCoche();
    controllerFavoritos.cargarTablaFavorito();
    controllerFurgoneta.cargarTablaFurgoneta();
    controllerMoto.cargarTablaMoto();
    controllerUsuario.cargarTablaUsuario();
    controllerVehiculo.cargarTablaVehiculo();
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
            alertas.mostrarAlerta("No se pudo cargar la pantalla de login.");
            return;
        }
    }
}

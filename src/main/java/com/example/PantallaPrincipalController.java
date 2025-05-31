package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.Controlador.Alertas;
import com.example.Controlador.Constantes;
import com.example.Controlador.ControllerAnuncio;
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
    @FXML private TableView<Coche> tableCoche;
    @FXML private TableView<Favorito> tableFavorito;
    @FXML private TableView<Furgoneta> tableFurgoneta;
    @FXML private TableView<Moto> tableMoto;
    @FXML private TableView<Usuario> tableUsuario;
    @FXML private TableView<Vehiculo> tableVehiculo;

    @FXML private TabPane tabPane;
    @FXML private HBox actionButtonsBox;

    @FXML private TextField buscadorUsuarioField;
    @FXML private Button buscarUsuarioButton;

private Connection connection;
private ControllerAnuncio controllerAnuncio;
private ControllerCoche controllerCoche;
private ControllerFavoritos controllerFavoritos;
private ControllerFurgoneta controllerFurgoneta;
private ControllerMoto controllerMoto;
private ControllerUsuario controllerUsuario;
private ControllerVehiculo controllerVehiculo;

private List<Usuario> listaUsuarios = new ArrayList<>(); // Debe contener la lista completa



    
    private  Alertas alertas = new Alertas();
  
public void initialize() throws ClassNotFoundException {
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

    exportarTXTButton.setOnAction(e -> exportarATXT());
     
}



    private void exportarATXT() {
        String outputDir = "C:/AutomarketTXTs";
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        try {
            exportarTablaTXT("Usuarios", tableUsuario.getItems(), outputDir + "/usuarios.txt");
            exportarTablaTXT("Vehículos", tableVehiculo.getItems(), outputDir + "/vehiculos.txt");
            exportarTablaTXT("Anuncios", tableAnuncio.getItems(), outputDir + "/anuncios.txt");
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
        Button buscarPorAñoBtn = new Button("Buscar por Año");
        Button buscarPorKilometrajeBtn = new Button("Buscar por Kilometraje");
        Button buscarPorMarcaBtn = new Button("Buscar por Marca");
        Button buscarPorUsuarioIdBtn = new Button("Buscar por Usuario ID");
        Button eliminarUsuarioIdBtn = new Button("Eliminar vehiculos sin usuario");


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
        buscarPorAñoBtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Buscar Vehículos por Año");
    dialog.setHeaderText("Buscar vehículos por año aproximado");
    dialog.setContentText("Introduce el año (±2 años):");

    dialog.showAndWait().ifPresent(input -> {
        try {
            int anio = Integer.parseInt(input);
            List<Vehiculo> vehiculos = controllerVehiculo.buscarVehiculosPorAnioAproximado(anio);

            if (vehiculos.isEmpty()) {
                alertas.mostrarAlerta("No se encontraron vehículos entre " + (anio - 2) + " y " + (anio + 2));
            } else {
                StringBuilder sb = new StringBuilder();
                for (Vehiculo v : vehiculos) {
                    sb.append("ID: ").append(v.getId())
                      .append(" | Marca: ").append(v.getMarca())
                      .append(" | Modelo: ").append(v.getModelo())
                      .append(" | Año: ").append(v.getAño())
                      .append("\n");
                }

                Alert resultado = new Alert(Alert.AlertType.INFORMATION);
                resultado.setTitle("Vehículos encontrados");
                resultado.setHeaderText("Vehículos entre " + (anio - 2) + " y " + (anio + 2));
                resultado.setContentText(sb.toString());
                resultado.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                resultado.showAndWait();
            }
        } catch (NumberFormatException ex) {
            alertas.mostrarAlerta("Año inválido. Introduce un número entero.");
        }
    });
});

// Buscar por kilometraje
buscarPorKilometrajeBtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Buscar por Kilometraje");
    dialog.setHeaderText("Buscar vehículos por kilometraje aproximado");
    dialog.setContentText("Introduce el kilometraje (±10000 km):");

    dialog.showAndWait().ifPresent(input -> {
        try {
            int km = Integer.parseInt(input);
            List<Vehiculo> vehiculos = controllerVehiculo.buscarVehiculosPorKilometrajeAproximado(km);

            if (vehiculos.isEmpty()) {
                alertas.mostrarAlerta("No se encontraron vehículos entre " + (km - 10000) + " y " + (km + 10000) + " km.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (Vehiculo v : vehiculos) {
                    sb.append("ID: ").append(v.getId())
                      .append(" | Marca: ").append(v.getMarca())
                      .append(" | Modelo: ").append(v.getModelo())
                      .append(" | Km: ").append(v.getKilometraje())
                      .append("\n");
                }

                Alert resultado = new Alert(Alert.AlertType.INFORMATION);
                resultado.setTitle("Resultados por Kilometraje");
                resultado.setHeaderText("Vehículos en rango de " + km + " km");
                resultado.setContentText(sb.toString());
                resultado.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                resultado.showAndWait();
            }
        } catch (NumberFormatException ex) {
            alertas.mostrarAlerta("Kilometraje inválido. Introduce un número entero.");
        }
    });
});

// Buscar por marca
buscarPorMarcaBtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Buscar por Marca");
    dialog.setHeaderText("Buscar vehículos por marca");
    dialog.setContentText("Introduce la marca (ej. Toyota):");

    dialog.showAndWait().ifPresent(input -> {
        List<Vehiculo> vehiculos = controllerVehiculo.buscarVehiculosPorMarca(input.trim());

        if (vehiculos.isEmpty()) {
            alertas.mostrarAlerta("No se encontraron vehículos con marca: " + input);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Vehiculo v : vehiculos) {
                sb.append("ID: ").append(v.getId())
                  .append(" | Marca: ").append(v.getMarca())
                  .append(" | Modelo: ").append(v.getModelo())
                  .append(" | Año: ").append(v.getAño())
                  .append("\n");
            }

            Alert resultado = new Alert(Alert.AlertType.INFORMATION);
            resultado.setTitle("Resultados por Marca");
            resultado.setHeaderText("Vehículos encontrados con marca: " + input);
            resultado.setContentText(sb.toString());
            resultado.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            resultado.showAndWait();
        }
    });
});

// Buscar por usuario ID
buscarPorUsuarioIdBtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Buscar por Usuario ID");
    dialog.setHeaderText("Buscar vehículos por ID de usuario");
    dialog.setContentText("Introduce el ID del usuario:");

    dialog.showAndWait().ifPresent(input -> {
        try {
            int userId = Integer.parseInt(input);
            List<Vehiculo> vehiculos = controllerVehiculo.buscarVehiculosPorUsuarioId(userId);

            if (vehiculos.isEmpty()) {
                alertas.mostrarAlerta("No se encontraron vehículos para el usuario ID: " + userId);
            } else {
                StringBuilder sb = new StringBuilder();
                for (Vehiculo v : vehiculos) {
                    sb.append("ID: ").append(v.getId())
                      .append(" | Marca: ").append(v.getMarca())
                      .append(" | Modelo: ").append(v.getModelo())
                      .append(" | Usuario ID: ").append(v.getUsuarioId())
                      .append("\n");
                }

                Alert resultado = new Alert(Alert.AlertType.INFORMATION);
                resultado.setTitle("Resultados por Usuario ID");
                resultado.setHeaderText("Vehículos del usuario ID: " + userId);
                resultado.setContentText(sb.toString());
                resultado.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                resultado.showAndWait();
            }
        } catch (NumberFormatException ex) {
            alertas.mostrarAlerta("ID inválido. Introduce un número entero.");
        }
    });
});
eliminarUsuarioIdBtn.setOnAction(e -> {
    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
    confirmacion.setTitle("Confirmar eliminación");
    confirmacion.setHeaderText("¿Eliminar todos los vehículos sin usuario asignado?");
    confirmacion.setContentText("Esta acción eliminará permanentemente todos los vehículos cuyo usuario_id sea 0.");

    Optional<ButtonType> resultado = confirmacion.showAndWait();
    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
        controllerVehiculo.eliminarVehiculosSinUsuario(() -> controllerVehiculo.cargarTablaVehiculo());
    }
});

        actionButtonsBox.getChildren().addAll(agregarVehiculoBtn, editarVehiculoBtn, eliminarVehiculoBtn,
            buscarPorAñoBtn, buscarPorKilometrajeBtn, buscarPorMarcaBtn, buscarPorUsuarioIdBtn, eliminarUsuarioIdBtn);
        break;
    }

    case "Anuncio": {
    Button agregarAnuncioBtn = new Button("Agregar Anuncio");
Button editarAnuncioBtn = new Button("Editar Anuncio");
Button eliminarAnuncioBtn = new Button("Eliminar Anuncio");
Button eliminarAnuncioPorIdbtn = new Button("Eliminar por ID");
Button buscarporVendedorIdbtn = new Button("Buscar por ID Vendedor");
Button buscarporPreciobtn = new Button("Buscar por Precio");

    // ✅ Lógica para buscar por precio


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

// ✅ Lógica para eliminar por ID
eliminarAnuncioPorIdbtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Eliminar Anuncio por ID");
    dialog.setHeaderText("Eliminar anuncio por ID");
    dialog.setContentText("Introduce el ID del anuncio a eliminar:");

    dialog.showAndWait().ifPresent(input -> {
        try {
            int id = Integer.parseInt(input);
            controllerAnuncio.eliminarAnuncioPorId(id, () -> controllerAnuncio.cargarTablaAnuncio());
        } catch (NumberFormatException ex) {
            alertas.mostrarAlerta("ID inválido. Introduce un número entero.");
        }
    });
});

// ✅ NUEVA lógica: Buscar por ID de Vendedor
buscarporVendedorIdbtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Buscar Anuncios por Vendedor");
    dialog.setHeaderText("Buscar anuncios por ID de vendedor");
    dialog.setContentText("Introduce el ID del vendedor:");

    dialog.showAndWait().ifPresent(input -> {
        try {
            int vendedorId = Integer.parseInt(input);
            List<Anuncio> anuncios = controllerAnuncio.buscarAnunciosPorVendedorId(vendedorId);

            if (anuncios.isEmpty()) {
                alertas.mostrarAlerta("No se encontraron anuncios para el vendedor con ID: " + vendedorId);
            } else {
                StringBuilder sb = new StringBuilder();
                for (Anuncio a : anuncios) {
                    sb.append("ID: ").append(a.getId())
                      .append(" | Vehículo ID: ").append(a.getVehiculoId())
                      .append(" | Precio: ").append(a.getPrecio())
                      .append(" | Descripción: ").append(a.getDescripcion())
                      .append("\n");
                }

                Alert resultado = new Alert(Alert.AlertType.INFORMATION);
                resultado.setTitle("Anuncios encontrados");
                resultado.setHeaderText("Anuncios del vendedor ID: " + vendedorId);
                resultado.setContentText(sb.toString());
                resultado.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                resultado.showAndWait();
            }
        } catch (NumberFormatException ex) {
            alertas.mostrarAlerta("ID inválido. Introduce un número entero.");
        }
    });
});
buscarporPreciobtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Buscar Anuncios por Precio");
    dialog.setHeaderText("Buscar anuncios en un rango de precio");
    dialog.setContentText("Introduce el precio de referencia:");

    dialog.showAndWait().ifPresent(input -> {
        try {
            double precio = Double.parseDouble(input);
            List<Anuncio> anuncios = controllerAnuncio.buscarAnunciosPorPrecioAproximado(precio);

            if (anuncios.isEmpty()) {
                alertas.mostrarAlerta("No se encontraron anuncios en el rango de ±1000€ del precio introducido.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (Anuncio a : anuncios) {
                    sb.append("ID: ").append(a.getId())
                      .append(" | Precio: ").append(a.getPrecio())
                      .append(" | Vehículo ID: ").append(a.getVehiculoId())
                      .append(" | Descripción: ").append(a.getDescripcion())
                      .append("\n");
                }

                Alert resultado = new Alert(Alert.AlertType.INFORMATION);
                resultado.setTitle("Resultados de búsqueda por precio");
                resultado.setHeaderText("Anuncios entre " + (precio - 1000) + "€ y " + (precio + 1000) + "€");
                resultado.setContentText(sb.toString());
                resultado.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                resultado.showAndWait();
            }
        } catch (NumberFormatException ex) {
            alertas.mostrarAlerta("Precio inválido. Introduce un número válido (ej. 9500.00)");
        }
    });
});

// ✅ Agregar todos los botones al panel
actionButtonsBox.getChildren().addAll(
    agregarAnuncioBtn, 
    editarAnuncioBtn, 
    eliminarAnuncioBtn, 
    eliminarAnuncioPorIdbtn, 
    buscarporVendedorIdbtn,
    buscarporPreciobtn
);
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
        break;
        }       


    }

    private void configurarColumnas() {
        configurarColumnasAnuncio();
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
            crearColumna("Vendedor ID", "vendedorId", 100)
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
    controllerCoche.cargarTablaCoche();
    controllerFavoritos.cargarTablaFavorito();
    controllerFurgoneta.cargarTablaFurgoneta();
    controllerMoto.cargarTablaMoto();
    controllerUsuario.cargarTablaUsuario();
    controllerVehiculo.cargarTablaVehiculo();
    }
@FXML
private void Backups(ActionEvent event) {
    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
    alerta.setTitle("Backup innecesario");
    alerta.setHeaderText("No es necesario hacer un backup");
    alerta.setContentText("Ya has ido escribiendo todo en la base de datos.");

    // Personaliza el tamaño si quieres que el texto se vea más grande
    alerta.getDialogPane().setStyle("-fx-font-size: 14pt;");

    alerta.showAndWait();
}


@FXML
private void exportarDatos(ActionEvent event) {
    try {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:automarket_.db");
        exportarDatosDesdeApp(connection);
        alertas.mostrarAlerta("Exportación completada exitosamente.");
        connection.close();
    } catch (Exception e) {
        e.printStackTrace();
        alertas.mostrarAlerta("Error al exportar los datos: " + e.getMessage());
    }
}

private void exportarDatosDesdeApp(Connection connection) {
    try {
        String userHome = System.getProperty("user.home");
        File desktop = new File(userHome, "Desktop");
        File backupsFolder = new File(desktop, "backups");
        if (!backupsFolder.exists()) backupsFolder.mkdir();

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        File backupSubfolder = new File(backupsFolder, timestamp);
        backupSubfolder.mkdir();

        // Exportación de tablas
        exportarTabla(connection, "anuncio", new String[]{"id", "vehiculo_id", "vendedor_id", "descripcion", "precio", "archivo_id"}, backupSubfolder, timestamp);
        // exportarTabla(connection, "archivo", new String[]{"id", "nombre", "anuncioId", "tipo"}, backupSubfolder, timestamp); // Descomenta si quieres usarla
        exportarTabla(connection, "coche", new String[]{"id", "carroceria"}, backupSubfolder, timestamp);
        exportarTabla(connection, "favorito", new String[]{"id", "comprador_id", "anuncioId"}, backupSubfolder, timestamp);
        exportarTabla(connection, "furgoneta", new String[]{"id", "capacidadCarga"}, backupSubfolder, timestamp);
        exportarTabla(connection, "moto", new String[]{"id", "cilindrada"}, backupSubfolder, timestamp);
        exportarTabla(connection, "usuario", new String[]{"id", "nombre", "email", "contrasenia"}, backupSubfolder, timestamp);
        exportarTabla(connection, "vehiculo", new String[]{"id", "marca", "modelo", "año", "kilometraje", "usuario_id"}, backupSubfolder, timestamp);

        System.out.println("Backup creado en: " + backupSubfolder.getAbsolutePath());

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void exportarTabla(Connection connection, String tabla, String[] columnas, File carpeta, String timestamp) {
    String nombreArchivo = tabla + "_" + timestamp + ".txt";
    File archivo = new File(carpeta, nombreArchivo);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo));
         Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla)) {

        writer.write(String.join(",", columnas));
        writer.newLine();

        while (rs.next()) {
            StringBuilder linea = new StringBuilder();
            for (int i = 0; i < columnas.length; i++) {
                String valor = rs.getString(columnas[i]);
                linea.append(valor != null ? valor : "");
                if (i < columnas.length - 1) linea.append(",");
            }
            writer.write(linea.toString());
            writer.newLine();
        }

        System.out.println("Exportado: " + archivo.getAbsolutePath());

    } catch (Exception e) {
        System.err.println("Error exportando tabla " + tabla + ": " + e.getMessage());
    }
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

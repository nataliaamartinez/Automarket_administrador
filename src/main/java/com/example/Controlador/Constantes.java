package com.example.Controlador;

public class Constantes {

    // Datos de conexión a la base de datos
    public static final String DB_URL = "jdbc:mysql://localhost:3306/automarket_?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = ""; // Establece aquí tu contraseña si tienes una

    // Driver JDBC
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Consultas SQL
    public static final String DB_QUERY_LOGIN = "SELECT * FROM usuario WHERE usuario = ? AND contrasena = ?";
    public static final String DB_QUERY_RECUPERACION = "SELECT correo FROM usuarios WHERE usuario = ?";
    public static final String DB_UPDATE_PASSWORD = "UPDATE usuarios SET contrasena = ? WHERE usuario = ?";
}

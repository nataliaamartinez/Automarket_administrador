package com.example.Modelo;

public class Usuario {
    private final Integer id;
    private final String nombre, email, contrasenia;

    public Usuario(int id, String nombre, String email, String contrasenia) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasenia = contrasenia;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getContrasenia() { return contrasenia; }

}

package com.example.Modelo;

public class Vehiculo {
    
    private final Integer id, anio, kilometraje, usuarioId;
    private final String marca, modelo;

    public Vehiculo(Integer id, String marca, String modelo, Integer anio, Integer kilometraje, Integer usuarioId) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.kilometraje = kilometraje;
        this.usuarioId = usuarioId;
    }

    public Integer getId() { return id; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public Integer getAnio() { return anio; }
    public Integer getKilometraje() { return kilometraje; }
    public Integer getUsuarioId() { return usuarioId; }
}

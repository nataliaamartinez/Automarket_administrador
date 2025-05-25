package com.example.Modelo;

public class Moto extends Vehiculo {
    private Integer cilindrada;

    // Constructor completo con todos los atributos
    public Moto(Integer id, String marca, String modelo, Integer anio, Integer kilometraje, Integer usuarioId, Integer cilindrada) {
        super(id, marca, modelo, anio, kilometraje, usuarioId);
        this.cilindrada = cilindrada;
    }

    // Constructor simplificado solo con id y cilindrada
    public Moto(Integer id, Integer cilindrada) {
        super(id, null, null, null, null, null); // Vehiculo con datos nulos
        this.cilindrada = cilindrada;
    }

    public Integer getCilindrada() {
        return cilindrada;
    }

    public void setCilindrada(Integer cilindrada) {
        this.cilindrada = cilindrada;
    }

    @Override
    public String toString() {
        return super.toString() + ", Moto{" +
                "cilindrada=" + cilindrada +
                '}';
    }
}

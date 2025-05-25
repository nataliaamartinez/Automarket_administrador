package com.example.Modelo;

public class Coche extends Vehiculo {

    private String carroceria;

    public Coche(Integer id, String marca, String modelo, Integer anio, Integer kilometraje, Integer usuarioId, String carroceria) {
        super(id, marca, modelo, anio, kilometraje, usuarioId);
        this.carroceria = carroceria;
    }

    // Constructor para solo id y carroceria
    public Coche(Integer id, String carroceria) {
        super(id, null, null, null, null, null);
        this.carroceria = carroceria;
    }

    public String getCarroceria() {
        return carroceria;
    }

    public void setCarroceria(String carroceria) {
        this.carroceria = carroceria;
    }

    @Override
    public String toString() {
        return super.toString() + ", Coche{" +
               "carroceria='" + carroceria + '\'' +
               '}';
    }
}

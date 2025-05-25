package com.example.Modelo;

public class Furgoneta extends Vehiculo {

    private Double capacidadcarga;

    public Furgoneta(Integer id, String marca, String modelo, Integer anio, Integer kilometraje, Integer usuarioId, Double capacidadcarga) {
        super(id, marca, modelo, anio, kilometraje, usuarioId);
        this.capacidadcarga = capacidadcarga;
    }

    // Constructor solo con id y capacidadcarga
    public Furgoneta(Integer id, Double capacidadcarga) {
        super(id, null, null, null, null, null);
        this.capacidadcarga = capacidadcarga;
    }

    public Double getCapacidadcarga() {
        return capacidadcarga;
    }

    public void setCapacidadcarga(Double capacidadcarga) {
        this.capacidadcarga = capacidadcarga;
    }

    @Override
    public String toString() {
        return super.toString() + ", Furgoneta{" +
               "capacidadcarga=" + capacidadcarga +
               '}';
    }
}

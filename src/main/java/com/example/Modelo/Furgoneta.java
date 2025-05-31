package com.example.Modelo;

public class Furgoneta extends Vehiculo {

    private Double capacidadCarga;

    public Furgoneta(Integer id, String marca, String modelo, Integer anio, Integer kilometraje, Integer usuarioId, Double capacidadCarga) {
        super(id, marca, modelo, anio, kilometraje, usuarioId);
        this.capacidadCarga = capacidadCarga;
    }

    // Constructor solo con id y capacidadcarga
    public Furgoneta(Integer id, Double capacidadCarga) {
        super(id, null, null, null, null, null);
        this.capacidadCarga = capacidadCarga;
    }

    public Double getCapacidadcarga() {
        return capacidadCarga;
    }

    public void setCapacidadcarga(Double capacidadcarga) {
        this.capacidadCarga = capacidadcarga;
    }

    @Override
    public String toString() {
        return super.toString() + ", Furgoneta{" +
               "capacidadcarga=" + capacidadCarga +
               '}';
    }
}

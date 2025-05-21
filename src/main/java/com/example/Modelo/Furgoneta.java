package com.example.Modelo;

public class Furgoneta {

        private final Integer id;
        private final Double capacidadCarga;

        public Furgoneta(int id, double capacidadCarga) {
            this.id = id;
            this.capacidadCarga = capacidadCarga;
        }
        public Integer getId() { return id; }
        public Double getCapacidadCarga() { return capacidadCarga; }
    }


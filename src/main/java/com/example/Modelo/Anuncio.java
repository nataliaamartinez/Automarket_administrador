package com.example.Modelo;

public class Anuncio {
       private final Integer id, vehiculoId, vendedorId, archivoId;
        private final Double precio;
        private final String descripcion;

        public Anuncio(int id, int vehiculoId, double precio, String descripcion, int vendedorId, Integer archivoId) {
            this.id = id;
            this.vehiculoId = vehiculoId;
            this.precio = precio;
            this.descripcion = descripcion;
            this.vendedorId = vendedorId;
            this.archivoId = archivoId;
        }
        public Integer getId() { return id; }
        public Integer getVehiculoId() { return vehiculoId; }
        public Double getPrecio() { return precio; }
        public String getDescripcion() { return descripcion; }
        public Integer getVendedorId() { return vendedorId; }
        public Integer getArchivoId() { return archivoId; }
    }


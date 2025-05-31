package com.example.Modelo;

public class Anuncio {
    private Integer id, vehiculoId, vendedorId;
    private Double precio;
    private String descripcion;

    public Anuncio(int id, int vehiculoId, double precio, String descripcion, int vendedorId ) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.precio = precio;
        this.descripcion = descripcion;
        this.vendedorId = vendedorId;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Integer vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(Integer vendedorId) {
        this.vendedorId = vendedorId;
    }

 
    @Override
    public String toString() {
        return "Anuncio{" +
                "id=" + id +
                ", vehiculoId=" + vehiculoId +
                ", vendedorId=" + vendedorId +
                ", precio=" + precio +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}

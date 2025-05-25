package com.example.Modelo;

     public class Archivo {
     
      private Integer id;
    private String archivoPath;

    public Archivo(int id, String archivoPath) {
        this.id = id;
        this.archivoPath = archivoPath;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getArchivoPath() {
        return archivoPath;
    }

    public void setArchivoPath(String archivoPath) {
        this.archivoPath = archivoPath;
    }

    @Override
    public String toString() {
        return "Archivo{id=" + id + ", archivoPath='" + archivoPath + "'}";
    }
}


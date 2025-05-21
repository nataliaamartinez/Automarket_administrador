package com.example.Modelo;

     public class Archivo {
     
        private final Integer id;
        private final String archivoPath;

    public Archivo(int id, String archivoPath) {
        this.id = id;
        this.archivoPath = archivoPath;
    }

    public Integer getId() { return id; }
    public String getArchivoPath() { return archivoPath; }
    
   
    @Override
public String toString() {
    return "Archivo{id=" + id + ", archivoPath='" + archivoPath + "'}";
}

}


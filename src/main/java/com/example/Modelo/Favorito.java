package com.example.Modelo;

public class Favorito {
     private Integer id;
    private Integer anuncioId;
    private Integer compradorId;

    public Favorito(int id, int anuncioId, int compradorId) {
        this.id = id;
        this.anuncioId = anuncioId;
        this.compradorId = compradorId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAnuncioId() {
        return anuncioId;
    }

    public void setAnuncioId(Integer anuncioId) {
        this.anuncioId = anuncioId;
    }

    public Integer getCompradorId() {
        return compradorId;
    }

    public void setCompradorId(Integer compradorId) {
        this.compradorId = compradorId;
    }
}
    


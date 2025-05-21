package com.example.Modelo;

public class Favorito {
          private final Integer id, anuncioId, compradorId;

    public Favorito(int id, int anuncioId, int compradorId) {
        this.id = id;
        this.anuncioId = anuncioId;
        this.compradorId = compradorId;
    }

    public Integer getId() { return id; }
    public Integer getAnuncioId() { return anuncioId; }
    public Integer getCompradorId() { return compradorId; }
    }

    


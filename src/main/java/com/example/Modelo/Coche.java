package com.example.Modelo;

  public  class Coche {
    
    private final Integer id;
    private final String carroceria;

    public Coche(int id, String carroceria) {
        this.id = id;
        this.carroceria = carroceria;
    }

    public Integer getId() { return id; }
    public String getCarroceria() { return carroceria; }
}
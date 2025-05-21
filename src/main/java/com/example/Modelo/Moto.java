package com.example.Modelo;

public class Moto {
    
        private final Integer id, cilindrada;

        public Moto(int id, int cilindrada) {
            this.id = id;
            this.cilindrada = cilindrada;
        }
        public Integer getId() { return id; }
        public Integer getCilindrada() { return cilindrada; }
    }

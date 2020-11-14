package com.mutual.emove.entidades;

import java.io.Serializable;

public class DiaActividad
        implements Serializable {
    private String dia;
    private String estado;

    public DiaActividad(String dia, String estado) {
        try {
            int ndia = Integer.parseInt(dia);
            if(ndia < 10){
                dia = "0"+ dia;
            }
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }

        this.dia = dia;
        this.estado=estado;
    }
    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

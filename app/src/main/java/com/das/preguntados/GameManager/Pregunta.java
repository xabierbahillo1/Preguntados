package com.das.preguntados.GameManager;

public class Pregunta {
    //Clase que contiene los datos de una pregunta
    private String generoPregunta;
    private String textoPregunta;
    private String textoOpcionA;
    private String textoOpcionB;
    private String textoOpcionC;
    private String opcionGanadora; //A, B o C

    public Pregunta(String generoPregunta, String textoPregunta, String textoOpcionA, String textoOpcionB, String textoOpcionC, String opcionGanadora) {
        this.generoPregunta = generoPregunta;
        this.textoPregunta = textoPregunta;
        this.textoOpcionA = textoOpcionA;
        this.textoOpcionB = textoOpcionB;
        this.textoOpcionC = textoOpcionC;
        this.opcionGanadora = opcionGanadora;
    }
    public String getGeneroPregunta(){
        return generoPregunta;
    }
    public String getTextoPregunta(){
        return textoPregunta;
    }

    public String getTextoOpcionA(){
        return textoOpcionA;
    }

    public String getTextoOpcionB(){
        return textoOpcionB;
    }

    public String getTextoOpcionC(){
        return textoOpcionC;
    }

    public String getOpcionGanadora(){
        return opcionGanadora;
    }

    public String generarStringPregunta(){
        //Genera el String con los datos de la pregunta separados por ;
        return getGeneroPregunta()+";"+getTextoPregunta()+";"+getTextoOpcionA()+";"+getTextoOpcionB()+";"+getTextoOpcionC()+";"+getOpcionGanadora();
    }
}

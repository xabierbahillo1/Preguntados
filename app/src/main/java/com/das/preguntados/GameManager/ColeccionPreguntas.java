package com.das.preguntados.GameManager;

import java.util.ArrayList;
import java.util.Random;

public class ColeccionPreguntas {
    //MAE que contiene la lista de preguntas

    private static ColeccionPreguntas miColeccion = new ColeccionPreguntas();
    private ArrayList<Pregunta> preguntas;
    private ColeccionPreguntas(){preguntas=new ArrayList<Pregunta>();}

    public static ColeccionPreguntas obtenerMiColeccion(){
        return miColeccion;
    }

    public void resetear(){
        //Resetea la lista de preguntas
        preguntas = new ArrayList<>();
    }

    public Pregunta obtenerPreguntaAlAzar(){
        //Devuelve una pregunta de la lista y la elimina
        if (preguntas.size()==0){ //Si no hay preguntas en la lista devuelvo null
            return null;
        }
        else {
           int index=new Random().nextInt(preguntas.size()); //Obtengo un numero al azar entre 0 y el numero de preguntas
            return preguntas.remove(index);
        }
    }

    public void anadirPregunta(Pregunta pPregunta){
        //Añade una pregunta a la lista
        preguntas.add(pPregunta);
    }
    public int obtenerNumeroPreguntas(){
        return preguntas.size();
    }
}

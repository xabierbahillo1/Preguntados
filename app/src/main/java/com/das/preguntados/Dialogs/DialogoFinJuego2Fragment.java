package com.das.preguntados.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.das.preguntados.R;


public class DialogoFinJuego2Fragment extends DialogFragment {
    /*MUESTRA EL MENSAJE FIN DE JUEGO DEL SEGUNDO MODO DE JUEGO*/
    private int puntuacionJuego;
    private int preguntasCorrectas;
    private int preguntasIncorrectas;
    public DialogoFinJuego2Fragment(int puntuacion, int correcto, int incorrecto) {
        puntuacionJuego=puntuacion;
        preguntasCorrectas=correcto;
        preguntasIncorrectas=incorrecto;
    }


    public Dialog onCreateDialog(Bundle savedInstanceState){
        return crearDialogoFinJuego();
    }

    private Dialog crearDialogoFinJuego(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater= getActivity().getLayoutInflater();
        View v= inflater.inflate(R.layout.fragment_dialogo_fin_juego2,null);
        builder.setView(v);
        //Obtengo referencias a los elementos del fragment
        TextView lpuntuacion= v.findViewById(R.id.textPuntuacion);
        TextView lpreguntasCorrectas=v.findViewById(R.id.textPreguntasAcertadas2);
        TextView lpreguntasIncorrectas=v.findViewById(R.id.textPreguntasIncorrectas2);
        //Muestro los datos obtenidos en el juego
        lpuntuacion.setText(getString(R.string.finjuego_puntuacionText1)+" "+puntuacionJuego+" "+getString(R.string.finjuego_puntuacionText2));

        if (preguntasCorrectas==1){
            lpreguntasCorrectas.setText(preguntasCorrectas+" "+getString(R.string.finjuego_respuestaCorrecta));
        }
        else{
            lpreguntasCorrectas.setText(preguntasCorrectas+" "+getString(R.string.finjuego_respuestasCorrectas));
        }

        if (preguntasIncorrectas==1){
            lpreguntasIncorrectas.setText(preguntasIncorrectas+" "+getString(R.string.finjuego_respuestaIncorrecta));
        }
        else{
            lpreguntasIncorrectas.setText(preguntasIncorrectas+" "+getString(R.string.finjuego_respuestasIncorrectas));
        }

        Button buttonSalir= v.findViewById(R.id.buttonCerrarDialog2);
        //Evento boton salir
        buttonSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return builder.create();
    }

    public void onAttach(Context context){
        super.onAttach(context);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_dialogo_fin_juego2,container,false);
    }
}
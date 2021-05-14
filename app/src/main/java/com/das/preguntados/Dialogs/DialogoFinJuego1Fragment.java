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


public class DialogoFinJuego1Fragment extends DialogFragment {
    /*MUESTRA EL MENSAJE FIN DE JUEGO DEL SEGUNDO MODO DE JUEGO*/
    private int preguntasCorrectas;

    public DialogoFinJuego1Fragment(int correcto) {
        preguntasCorrectas=correcto;
    }


    public Dialog onCreateDialog(Bundle savedInstanceState){
        return crearDialogoFinJuego();
    }

    private Dialog crearDialogoFinJuego(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater= getActivity().getLayoutInflater();
        View v= inflater.inflate(R.layout.fragment_dialogo_fin_juego1,null);
        builder.setView(v);
        //Obtengo referencias a los elementos del fragment
        TextView lpreguntas=v.findViewById(R.id.textPreguntasAcertadas1);

        //Muestro los datos obtenidos en el juego
        if (preguntasCorrectas==0){
            lpreguntas.setText(getString(R.string.finjuego1_noAciertos));
        }
        else if (preguntasCorrectas==1){
            lpreguntas.setText(getString(R.string.finJuego1_respuestaCorrecta));
        }
        else{
            lpreguntas.setText(getString(R.string.finJuego1_respuestasCorrectas1)+" "+preguntasCorrectas+" "+getString(R.string.finJuego1_respuestasCorrectas2));
        }

        Button buttonSalir= v.findViewById(R.id.buttonCerrarDialog1);
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
        return inflater.inflate(R.layout.fragment_dialogo_fin_juego1,container,false);
    }
}
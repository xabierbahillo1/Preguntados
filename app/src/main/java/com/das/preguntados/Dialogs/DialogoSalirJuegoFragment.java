package com.das.preguntados.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.das.preguntados.R;

public class DialogoSalirJuegoFragment extends DialogFragment {
    /*MUESTRA EL MENSAJE PARA SALIR DEL JUEGO AL PULSAR ATRAS EN GAMEACTIVITY*/

    ListenerDialogoSalirJuego miListener;
    public interface ListenerDialogoSalirJuego {
        //Listener para llamar al metodo abandonarJuego de GameActivity
        void abandonarJuego();
    }

    public DialogoSalirJuegoFragment() { }


    public Dialog onCreateDialog(Bundle savedInstanceState){
        miListener= (ListenerDialogoSalirJuego) getActivity();
        return crearDialogoSalirJuego();
    }

    private Dialog crearDialogoSalirJuego(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater= getActivity().getLayoutInflater();
        View v= inflater.inflate(R.layout.fragment_dialogo_salir_juego,null);
        builder.setView(v);
        //Obtengo referencias a los elementos del fragment
        Button buttonCancelar= v.findViewById(R.id.buttonCancelarSalir);
        Button buttonAbandonar= v.findViewById(R.id.buttonAbandonar);
        //Evento boton cancelar
        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(); //Cierro el dialog
            }
        });
        buttonAbandonar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                miListener.abandonarJuego();
                dismiss(); //Cierro el dialog
            }
        });

        return builder.create();
    }

    public void onAttach(Context context){
        super.onAttach(context);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_dialogo_salir_juego,container,false);
    }
}
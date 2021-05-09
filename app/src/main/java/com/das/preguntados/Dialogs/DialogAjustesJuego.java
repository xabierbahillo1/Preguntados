package com.das.preguntados.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.das.preguntados.R;

import java.util.Arrays;


public class DialogAjustesJuego extends DialogFragment {
    //Clase generica para mostrar un Dialog informativo:
    // Parametros: Titulo, Mensaje
    private boolean[] selections;
    DialogAjustesJuego.ListenerDialogoAjustesJuego miListener;
    public interface ListenerDialogoAjustesJuego {
        //Listener para llamar al metodo abandonarJuego de GameActivity
        void guardarNuevosAjustes(boolean[] seleccion, String seleccionString);
    }

    public DialogAjustesJuego() {
        super();
    }

    public static DialogAjustesJuego newInstance(boolean[] myselections) {
        //Metodo factoria para instanciar un Dialog. Recibe como parametros el titulo y mensaje del dialog
        Bundle args = new Bundle();
        //args.putString("title", title);
        //args.putString("message", message);
        boolean[] lasSelecciones= Arrays.copyOf(myselections,myselections.length); //Copia del array para que no se guarden directamente los cambios
        args.putBooleanArray("selections",lasSelecciones);

        DialogAjustesJuego elDialog = new DialogAjustesJuego();
        elDialog.setArguments(args);
        return elDialog;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Crea el dialog
        super.onCreateDialog(savedInstanceState);
        miListener= (DialogAjustesJuego.ListenerDialogoAjustesJuego) getActivity();
        selections=getArguments().getBooleanArray("selections");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.ajustes_Title));

        LayoutInflater inflater=getActivity().getLayoutInflater();
        View elaspecto= inflater.inflate(R.layout.dialogo_ajustes_layout,null);
        builder.setView(elaspecto);


        //builder.setMessage(getString(R.string.ajustes_Aviso));
        String[] list={getString(R.string.opcion_Deporte),getString(R.string.opcion_Arte),getString(R.string.opcion_Geografia),getString(R.string.opcion_Historia),getString(R.string.opcion_Ciencia),getString(R.string.opcion_Entretenimiento)};

        builder.setMultiChoiceItems(list, selections, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b){
                    selections[i]=true;
                }
                else{
                    selections[i]=false;
                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.alert_btnGuardar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Al pulsar el botón guardar, se envian los nuevos ajustes a la pantalla
                String result="";
                int countTrues=0;
                for (int y=0;y<selections.length;y++){
                        boolean selection=selections[y];
                        if (selection==true){ //Si esta marcado
                            if (countTrues==0){
                                result=list[y];
                            }
                            else{
                                result+=","+list[y];
                            }
                            countTrues++;
                        }

                }
                if (countTrues==selections.length){
                    result=null;
                }
                if (countTrues!=0){
                    Toast.makeText(getActivity(),getString(R.string.ajustes_guardados),Toast.LENGTH_SHORT).show();
                    miListener.guardarNuevosAjustes(selections,result);
                }
                else {
                    Toast.makeText(getActivity(),getString(R.string.ajustes_NoSeleccion),Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.alert_btnCancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Al pulsar el botón cancelar se cierra el Dialog sin realizar ninguna acción
            }
        });
        return builder.create();
    }
}

package com.das.preguntados.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.das.preguntados.R;

public class DialogNoConexion extends DialogFragment {
    //Clase generica para mostrar un dialog de fallo de conexion

    public DialogNoConexion() {
        super();
    }

    public static DialogNoConexion newInstance() {
        //Metodo factoria para instanciar un Dialog.
        DialogNoConexion elDialog = new DialogNoConexion();
        return elDialog;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Crea el dialog
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.noConexion_text));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.alert_btnContinuar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Al pulsar el botón continuar se cierra la actividad
                getActivity().finish();
            }
        });
        return builder.create();
    }
}

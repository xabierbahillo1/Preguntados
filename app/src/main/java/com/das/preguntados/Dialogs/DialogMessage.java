package com.das.preguntados.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.das.preguntados.R;

public class DialogMessage extends DialogFragment {
    //Clase generica para mostrar un Dialog informativo:
    // Parametros: Titulo, Mensaje

    public DialogMessage() {
        super();
    }

    public static DialogMessage newInstance(String title, String message) {
        //Metodo factoria para instanciar un Dialog. Recibe como parametros el titulo y mensaje del dialog
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        DialogMessage elDialog = new DialogMessage();
        elDialog.setArguments(args);
        return elDialog;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Crea el dialog
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("message"));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.alert_btnContinuar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Al pulsar el botón continuar se cierra el Dialog sin realizar ninguna acción.
            }
        });
        return builder.create();
    }
}

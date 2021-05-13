package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.das.preguntados.R;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        String nombre = getIntent().getExtras().getString("nombre");
        String usuario = getIntent().getExtras().getString("usuario");
        String email = getIntent().getExtras().getString("email");
        String foto = getIntent().getExtras().getString("foto");

        ImageView imgPerfilEdit = findViewById(R.id.imgPerfilEdit);
        byte[] decodedString = Base64.decode(foto, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imgPerfilEdit.setImageBitmap(decodedByte);

        EditText editTextNombre = findViewById(R.id.editTextNombre);
        editTextNombre.setText(nombre);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        editTextEmail.setText(email);

        Button btnAceptarPerfil = findViewById(R.id.btnAceptarPerfil);
        btnAceptarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage(R.string.textCancelarEditarPerfil).setPositiveButton(R.string.textSi, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si dice que sí, se cierra la actividad
                finish();
            }
        }).setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si dice que no, se cierra el Dialog y no hace nada
            }
        });
        builder.show();
    }
}
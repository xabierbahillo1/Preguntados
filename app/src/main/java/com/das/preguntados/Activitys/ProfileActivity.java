package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.das.preguntados.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String fotoAux = getIntent().getExtras().getString("foto");
        String foto = fotoAux.replace(" ", "+");

        String nombre = getIntent().getExtras().getString("nombre");
        String usuario = getIntent().getExtras().getString("usuario");
        String email = getIntent().getExtras().getString("email");

        ImageView imgPerfil = findViewById(R.id.imgPerfil);
        byte[] decodedString = Base64.decode(foto, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imgPerfil.setImageBitmap(decodedByte);

        TextView textNombre = findViewById(R.id.textNombre);
        textNombre.setText(nombre);

        TextView textUsuario = findViewById(R.id.textUsuario);
        textUsuario.setText(usuario);

        TextView textEmail = findViewById(R.id.textEmail);
        textEmail.setText(email);

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Finalizo mi actividad
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        Button btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra("foto", foto);
                i.putExtra("nombre", nombre);
                i.putExtra("usuario", usuario);
                i.putExtra("email", email);
                startActivity(i);
            }
        });
    }
}
package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.das.preguntados.Common.generateAuthToken;
import com.das.preguntados.R;
import com.das.preguntados.WS.obtenerDatosUsuarioWS;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ProfileActivity extends AppCompatActivity {

    private String usuario;
    private String nombre;
    private String email;
    private String foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        String usuario = getIntent().getExtras().getString("usuario");

        TextView textUsuario = findViewById(R.id.textUsuario);
        textUsuario.setText(usuario);

        obtenerDatos(usuario);

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setMessage(R.string.textCerrarSesion).setPositiveButton(R.string.textSi, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish(); //Finalizo mi actividad
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                    }
                }).setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // No hacer nada, el usuario ha cancelado el Dialog
                    }
                });
                builder.show();

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

    private void obtenerDatos(String usuario) {
        Data datos = new Data.Builder()
                .putString("usuario",usuario)
                .build();

        OneTimeWorkRequest obtenerDatosOtwr= new OneTimeWorkRequest.Builder(obtenerDatosUsuarioWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(obtenerDatosOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta, teniendo en cuenta que es de la forma CODIGO#VALOR
                        if(workInfo != null && workInfo.getState().isFinished()){
                            Data outputData = workInfo.getOutputData();
                            String resultado = outputData.getString("resultado");
                            if (resultado!=null) {
                                JSONParser parser = new JSONParser();
                                JSONObject json = null;
                                try {
                                    json = (JSONObject) parser.parse(resultado);
                                    ProfileActivity.this.nombre=(String) json.get("nombreCompleto");
                                    ProfileActivity.this.email=(String) json.get("email");
                                    String fotoAux=(String) json.get("foto");
                                    assert fotoAux != null;
                                    ProfileActivity.this.foto = fotoAux.replace(" ", "+");

                                    ImageView imgPerfil = findViewById(R.id.imgPerfil);
                                    byte[] decodedString = Base64.decode(foto, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    imgPerfil.setImageBitmap(decodedByte);

                                    TextView textNombre = findViewById(R.id.textNombre);
                                    textNombre.setText(nombre);

                                    TextView textEmail = findViewById(R.id.textEmail);
                                    textEmail.setText(email);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                                    finish(); //Finalizo actividad actual
                                    Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
                                    i.putExtra("usuario",usuario); //Envio los datos del usuario
                                    startActivity(i);
                                }
                            }

                        }
                    });
        WorkManager.getInstance(getApplicationContext()).enqueue(obtenerDatosOtwr);
    }
}
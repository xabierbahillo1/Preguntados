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
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.das.preguntados.Common.generateAuthToken;
import com.das.preguntados.R;
import com.das.preguntados.WS.ActualizarPerfilWS;
import com.das.preguntados.WS.registroWS;

public class EditProfileActivity extends AppCompatActivity {

    Bitmap bitmap = null;

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
        EditProfileActivity.this.bitmap = decodedByte;
        imgPerfilEdit.setImageBitmap(decodedByte);

        String newFoto = null;

        imgPerfilEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentFoto, 14);
            }
        });



        EditText editTextNombre = findViewById(R.id.editTextNombre);
        editTextNombre.setText(nombre);

        EditText editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextUsuario.setText(usuario);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        editTextEmail.setText(email);

        Button btnAceptarPerfil = findViewById(R.id.btnAceptarPerfil);
        btnAceptarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNombre = editTextNombre.getText().toString().trim();
                String newUsuario = editTextUsuario.getText().toString().trim();
                String newEmail = editTextEmail.getText().toString().trim();
                Data datos = new Data.Builder()
                        .putString("nombre", newNombre)
                        .putString("usuario", newUsuario)
                        .putString("email", newEmail)
                        .putString("newFoto", newFoto)
                        .build();
                OneTimeWorkRequest registerOtwr= new OneTimeWorkRequest.Builder(ActualizarPerfilWS.class).setInputData(datos)
                        .build();

                WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(registerOtwr.getId())
                        .observe(EditProfileActivity.this, new Observer<WorkInfo>() {
                            @Override
                            public void onChanged(WorkInfo workInfo) {
                                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                i.putExtra("foto", newFoto);
                                i.putExtra("nombre", newNombre);
                                i.putExtra("usuario", newUsuario);
                                i.putExtra("email", newEmail);
                                startActivity(i);
                            }
                        });
                WorkManager.getInstance(getApplicationContext()).enqueue(registerOtwr);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 14 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap laminiatura = (Bitmap) extras.get("data");
            this.bitmap = laminiatura;
            // Añadir el BitMap al imageView
            ImageView imgPreview = findViewById(R.id.imgPerfilEdit);
            imgPreview.setImageBitmap(laminiatura);
        }
    }
}
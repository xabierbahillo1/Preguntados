package com.das.preguntados.Activitys;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.das.preguntados.Common.ActivityVertical;
import com.das.preguntados.Dialogs.DialogMessage;
import com.das.preguntados.R;
import com.das.preguntados.WS.ActualizarPerfilWS;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class EditProfileActivity extends ActivityVertical {


    String usuario;
    String imgUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (android.os.Build.VERSION.SDK_INT > 9) { //Permito descargas en primer plano
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        String nombre = getIntent().getExtras().getString("nombre");
        usuario = getIntent().getExtras().getString("usuario");
        String email = getIntent().getExtras().getString("email");

        //Cargo la foto de perfil
        String foto = getIntent().getExtras().getString("foto");

        ImageView imgPerfilEdit = findViewById(R.id.imgPerfilEdit);
        if (foto!=null && foto!=""){
            String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/"+foto;
            URL destino = null;
            try {
                destino = new URL(direccion);
                HttpURLConnection conn = (HttpURLConnection) destino.openConnection();
                int responseCode = 0;
                responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    Bitmap elBitmap = BitmapFactory.decodeStream(conn.getInputStream()); //Obtengo la imagen
                    imgPerfilEdit.setImageBitmap(elBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else { //Foto de perfil por defecto
            imgPerfilEdit.setImageDrawable(getDrawable(R.drawable.imgavatar));
        }

        String newFoto = null;

        imgPerfilEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gestionarSubirFoto();
            }
        });

        TextView cambiarFoto= findViewById(R.id.cambiarFotoText);
        cambiarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gestionarSubirFoto();
            }
        });

        EditText editTextNombre = findViewById(R.id.editTextNombre);
        editTextNombre.setText(nombre);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        editTextEmail.setText(email);

        Button btnAceptarPerfil = findViewById(R.id.btnAceptarPerfil);
        btnAceptarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNombre = editTextNombre.getText().toString().trim();
                String newEmail = editTextEmail.getText().toString().trim();

                if (newNombre.isEmpty() || newEmail.isEmpty()){ //Si algun campo es vacio
                    DialogFragment dialogoError= DialogMessage.newInstance(getString(R.string.editPerfil_errorTitle),getString(R.string.register_error_camposObligatorios));
                    dialogoError.show(getSupportFragmentManager(), "errorRegistro");
                    //Muestro el error en los logs
                    Log.d("editProfile",getString(R.string.register_error_camposObligatorios));
                }
                else if (!comprobarCorreo(newEmail)){ //Si el correo no es correcto
                    DialogFragment dialogoError= DialogMessage.newInstance(getString(R.string.editPerfil_errorTitle),getString(R.string.register_error_correoIncorrecto));
                    dialogoError.show(getSupportFragmentManager(), "errorRegistro");
                    //Muestro el error en los logs
                    Log.d("editProfile",getString(R.string.register_error_correoIncorrecto));
                }
                else {

                    Data datos = new Data.Builder()
                            .putString("nombre", newNombre)
                            .putString("usuario", usuario)
                            .putString("email", newEmail)
                            .putString("foto", imgUri)
                            .build();
                    OneTimeWorkRequest registerOtwr = new OneTimeWorkRequest.Builder(ActualizarPerfilWS.class).setInputData(datos)
                            .build();

                    WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(registerOtwr.getId())
                            .observe(EditProfileActivity.this, new Observer<WorkInfo>() {
                                @Override
                                public void onChanged(WorkInfo workInfo) {
                                    //Trato la respuesta, teniendo en cuenta que es de la forma CODIGO#VALOR
                                    if(workInfo != null && workInfo.getState().isFinished()){

                                        String resultado=workInfo.getOutputData().getString("resultado");
                                        if (resultado!=null) {
                                            String[] respuesta= resultado.split("#");
                                            if (respuesta[0].equals("ERR")){ //Hay un error
                                                DialogFragment dialogoError= DialogMessage.newInstance(getString(R.string.editPerfil_errorTitle),gestionarMensajesError(respuesta[1]));
                                                dialogoError.show(getSupportFragmentManager(), "errorRegistro");
                                                //Muestro el error en los logs
                                                Log.d("editProfile",gestionarMensajesError(respuesta[1]));

                                            }
                                            else if (respuesta[0].equals("OK")){ //Registro completado
                                                Log.d("editProfile","Datos guardados correctamente");
                                                finish();
                                                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                                i.putExtra("usuario", usuario);
                                                startActivity(i);
                                            }
                                        }

                                    }
                                }
                            });
                    WorkManager.getInstance(getApplicationContext()).enqueue(registerOtwr);
                }
            }
        });
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setMessage(R.string.textCancelarEditarPerfil).setPositiveButton(R.string.textSi, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si dice que sí, se cierra la actividad
                finish(); //Finalizo la actividad
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                i.putExtra("usuario", usuario);
                startActivity(i);
            }
        }).setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si dice que no, se cierra el Dialog y no hace nada
            }
        });
        builder.show();
    }
    private boolean comprobarCorreo(String correo){
        //Valida si un email es correcto o no.
        Pattern patronEmail = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mather = patronEmail.matcher(correo);
        return mather.find();
    }

    private void gestionarSubirFoto(){
        Intent intentFoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intentFoto, 14);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 14 && resultCode == RESULT_OK) {
            Uri imagenSeleccionada = data.getData();
            ImageView imgPreview = findViewById(R.id.imgPerfilEdit);
            imgPreview.setImageURI(imagenSeleccionada);
            imgUri=imagenSeleccionada.toString();

        }
    }

    private String gestionarMensajesError(String message){
        //Gestiona los mensajes de error recibidos en la respuesta
        if (message.equals("register_error_conexionBD")) {
            return getString(R.string.register_error_conexionBD);
        }
        if (message.equals("register_error_consultaBD")){
            return getString(R.string.register_error_consultaBD);
        }
        if (message.equals("register_error_correoExiste")){
            return getString(R.string.register_error_correoExiste);
        }
        if (message.equals("register_error_camposObligatorios")){
            return getString(R.string.register_error_camposObligatorios);
        }
        return "Ha sucedido un error al guardar";
    }

}
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

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;

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

        //Obtengo los datos enviados a la actividad como parametro
        String nombre = getIntent().getExtras().getString("nombre");
        usuario = getIntent().getExtras().getString("usuario");
        String email = getIntent().getExtras().getString("email");
        String foto = getIntent().getExtras().getString("foto");

        //Cargo la foto de perfil
        ImageView imgPerfilEdit = findViewById(R.id.imgPerfilEdit);
        if (foto!=null && foto!=""){ //Si hay foto la descargo del servidor
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

        // -- GESTION EVENTO CAMBIAR FOTO DE PERFIL --
        String newFoto = null; //Guarda la referencia a la nueva foto

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
        // --------------------------------------------

        //Introduzco en los editText los valores de nombreCompleto y email del usuario
        EditText editTextNombre = findViewById(R.id.editTextNombre);
        editTextNombre.setText(nombre);

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        editTextEmail.setText(email);

        //Gestion boton guardar cambios
        Button btnAceptarPerfil = findViewById(R.id.btnAceptarPerfil);
        btnAceptarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtengo el nuevo nombre y nuevo email
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
                else { //Correcto, envio los nuevos datos a la base de datos

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
                                                dialogoError.show(getSupportFragmentManager(), "errorEditProfile");
                                                //Muestro el error en los logs
                                                Log.d("editProfile",gestionarMensajesError(respuesta[1]));

                                            }
                                            else if (respuesta[0].equals("OK")){ //Cambios guardados
                                                Log.d("editProfile","Datos guardados correctamente");
                                                //Muestro un dialog indicando que se han guardado los datos correctamente
                                                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                                builder.setMessage(R.string.editPerfil_guardadoOk).setPositiveButton(R.string.editPerfil_continuar, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Pulsa continuar, finalizo la actividad y vuelvo a el perfil
                                                        finish();
                                                        Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                                        i.putExtra("usuario", usuario);
                                                        startActivity(i);
                                                    }
                                                });
                                                builder.show();
                                            }
                                        }
                                        else { //No conexion
                                            DialogFragment dialogoError= DialogMessage.newInstance(getString(R.string.editPerfil_errorTitle),getString(R.string.register_error_conexionBD));
                                            dialogoError.show(getSupportFragmentManager(), "errorEditProfile");
                                            //Muestro el error en los logs
                                            Log.d("editProfile",getString(R.string.register_error_conexionBD));
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
        //Pulsa el boton volver, se muestra un mensaje indicando que no se guardaran los cambios realizados
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
        //Lanza un intent para obtener una foto de la galeria
        Intent intentFoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intentFoto, 14);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 14 && resultCode == RESULT_OK) {
            //Obtengo la imagen seleccionada de la galeria
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
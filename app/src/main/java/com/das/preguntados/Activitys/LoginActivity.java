package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.das.preguntados.Common.ActivityVertical;
import com.das.preguntados.Common.generateAuthToken;
import com.das.preguntados.Dialogs.DialogMessage;
import com.das.preguntados.R;
import com.das.preguntados.WS.iniciarSesionWS;


public class LoginActivity extends ActivityVertical {
    /*PANTALLA DE LOGIN. Permite al usuario iniciar sesión con un usuario registrado en el sistema y pasar a MainMenuActivity
    o pasar a la actividad RegistroActivity para registrar un usuario
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        //Gestion de eventos
        gestionarRegisterText();
        gestionarLoginButton();

    }


    private void gestionarRegisterText(){
        //Gestiona los eventos del textView registerText
        TextView textRegister= findViewById(R.id.registerText);
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lanzar actividad registro
                finish(); //Finalizo mi actividad
                Intent i = new Intent(getApplicationContext(), RegistroActivity.class);
                startActivity(i);
            }
        });
    }

    private void gestionarLoginButton(){
        //Gestiona los eventos del boton buttonLogin
        Button buttonLogin= findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Obtengo los datos del usuario y contraseña introducidos
                EditText usuarioET=findViewById(R.id.userText);
                EditText passwordET=findViewById(R.id.claveText);
                String usuario=usuarioET.getText().toString();
                String password=passwordET.getText().toString();
                if (usuario.isEmpty() || password.isEmpty()){ //Si alguno de los campos es vacio, muestro mensaje de error
                    gestionarError(getString(R.string.login_error_camposObligatorios));
                }
                else {
                    gestionarInicioSesion(usuario,password);
                }
            }
        });
    }

    private void gestionarInicioSesion(String usuario, String clave){
        // Inicio de sesion manual
         /* Genero un token de alguna manera para guardar el login, y envio a base de datos usuario, clave y el token.
          Si es login correcto, guardo el token en la base de datos y en sharedPreferences, finalizo esta actividad y paso a
          la actividad principal.
        */
        //Genero un token para guardar el login
        String token=generateAuthToken.generateToken();
        //Peticion contra BD para comprobar el login
        Log.d("login","Verificaciones correctas. Comienza petición a base de datos para login");

        Data datos = new Data.Builder()
                .putString("usuario",usuario)
                .putString("clave",clave)
                .putString("token",token)
                .build();

        OneTimeWorkRequest iniciarSesionOtwr= new OneTimeWorkRequest.Builder(iniciarSesionWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(iniciarSesionOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta, teniendo en cuenta que es de la forma CODIGO#VALOR
                        if(workInfo != null && workInfo.getState().isFinished()){

                            String resultado=workInfo.getOutputData().getString("resultado");
                            if (resultado!=null) {
                                String[] respuesta= resultado.split("#");
                                if (respuesta[0].equals("ERR")){ //Hay un error
                                    gestionarError(gestionarMensajesError(respuesta[1]));
                                }
                                else if (respuesta[0].equals("OK")){
                                    //Datos de login correctos, guardo token en preferencias y lanzo la actividad principal
                                    Log.d("login","Iniciada sesion con usuario"+usuario);
                                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(generateAuthToken.AUTH_CONTEXT, Context.MODE_PRIVATE); //Obtengo las preferencias de autenticacion
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("usuario", usuario);
                                    editor.putString("token",token);
                                    editor.commit();
                                    finish(); //Finalizo actividad actual
                                    Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
                                    i.putExtra("usuario",usuario); //Envio los datos del usuario
                                    startActivity(i);
                                }
                            }

                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(iniciarSesionOtwr);
    }

    private void gestionarError(String message){
        //Muestra un dialog con el error indicado
        DialogFragment dialogoError= DialogMessage.newInstance(getString(R.string.login_alertTitle),message);
        dialogoError.show(getSupportFragmentManager(), "errorLogin");
        //Muestro el error en los logs
        Log.d("login",message);
    }

    private String gestionarMensajesError(String message){
        //Gestiona los mensajes de error recibidos en la respuesta
        if (message.equals("login_error_conexionBD")) {
            return getString(R.string.login_error_conexionBD);
        }
        if (message.equals("login_error_consultaBD")){
            return getString(R.string.login_error_consultaBD);
        }
        if (message.equals("login_error_camposObligatorios")){
            return getString(R.string.login_error_camposObligatorios);
        }
        if (message.equals("login_error_authIncorrecta")) {
            return getString(R.string.login_error_authIncorrecta);
        }

        return "Ha sucedido un error al iniciar sesion";
    }
}
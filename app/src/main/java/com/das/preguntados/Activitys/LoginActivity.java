package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.das.preguntados.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //TODO: Login automatico
        /* Compruebo si tengo un usuario y token guardados en SharedPreferences, si tengo
           lanzo una peticion contra BD para comprobar si son datos correctos:
             - Si son correctos, finalizo esta actividad y paso a la actividad principal
             - Si no, continuo con el onCreate y muestro un mensaje de sesión caducada
         */

        //Gestion de eventos
        gestionarRegisterText();
        gestionarLoginButton();


    }

    private void gestionarInicioSesion(String usuario, String clave){
        /*TODO: Inicio de sesion manual
          Genero un token de alguna manera para guardar el login, y envio a base de datos usuario, clave y el token.
          Si es login correcto, guardo el token en la base de datos y en sharedPreferences, finalizo esta actividad y paso a
          la actividad principal.
        */

        finish(); //Finalizo mi actividad
        Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
        i.putExtra("usuario","admin"); //Por defecto le paso un usuario administrador
        startActivity(i);
    }

    private void gestionarRegisterText(){
        //Gestiona los eventos del textView registerText
        TextView textRegister= findViewById(R.id.registerText);
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lanzar actividad registro
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
                gestionarInicioSesion("admin","password");
            }
        });
    }

}
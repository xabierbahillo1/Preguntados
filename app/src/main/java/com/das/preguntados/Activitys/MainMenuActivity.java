package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.das.preguntados.R;

public class MainMenuActivity extends AppCompatActivity {
    /*
           MENU PRINCIPAL:
            - ACCEDER A LA ACTIVIDAD SELECCION DE MODO DE JUEGO
                - JUGAR AL MODO RESPONDER MAXIMO DE PREGUNTAS POSIBLES SIN FALLAR
                - JUGAR AL MODO RESPONDER MAXIMO DE PREGUNTAS POSIBLES EN UN TIEMPO DETERMINADO
                - JUGAR AL MODO DUELO (RETAR A UN AMIGO QUE TENGAS AGREGADO) <- SI DA TIEMPO
                - PREFERENCIAS DE JUEGO (DIFICULTAD,TEMATICA)
            - GESTIONAR PERFIL (AÑADIR FOTO DE PERFIL, AÑADIR AMIGOS, VER ESTADISTICA PREGUNTAS CORRECTAS/INCORRECTAS, DUELOS GANADOS, CERRAR SESION...)
            - ACCEDER AL RANKING (GLOBAL Y POR AMIGOS)
     */

    private String usuario; //Referencia al usuario que ha iniciado sesion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu_activity);

        //Obtengo el usuario que ha iniciado sesion
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            usuario=extras.getString("usuario");
        }
        Intent i = new Intent(getApplicationContext(), GameSelectorActivity.class);
        startActivity(i);

    }
}
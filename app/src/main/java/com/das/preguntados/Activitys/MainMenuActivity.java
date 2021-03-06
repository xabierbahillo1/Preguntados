package com.das.preguntados.Activitys;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.das.preguntados.Common.ActivityVertical;
import com.das.preguntados.R;

public class MainMenuActivity extends ActivityVertical {
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
        // Funcionalidad de los botones
        Button btnJugar = findViewById(R.id.btnJugar);
        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GameSelectorActivity.class);
                i.putExtra("usuario",usuario);
                startActivity(i);
            }
        });

        Button btnRanking = findViewById(R.id.btnRanking);
        btnRanking.setText(R.string.btnRanking);
        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RankingActivity.class);
                startActivity(i);
            }
        });

        Button btnPerfil = findViewById(R.id.btnPerfil);
        btnPerfil.setText(R.string.btnPerfil);
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Finalizo la actividad
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                i.putExtra("usuario", MainMenuActivity.this.usuario);
                startActivity(i);
            }
        });


    }
}
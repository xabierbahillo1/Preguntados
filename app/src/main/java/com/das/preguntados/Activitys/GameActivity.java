package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.das.preguntados.R;

public class GameActivity extends AppCompatActivity {
    /*ACTIVIDAD QUE GESTIONA EL JUEGO*/
    /*LAS PREGUNTAS YA ESTAN CARGADAS EN LA MAE LISTAPREGUNTAS*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

    }
}
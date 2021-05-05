package com.das.preguntados;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.das.preguntados.Activitys.GameActivity;
import com.das.preguntados.GameManager.ColeccionPreguntas;
import com.das.preguntados.WS.obtenerPreguntasWS;

public class GameSelectorActivity extends AppCompatActivity {
    /*
        ACTIVIDAD PARA ELEGIR EL MODO DE JUEGO A JUGAR
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selector);

        Button buttonModo1= findViewById(R.id.buttonModo1);
        buttonModo1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Se lanza actividad con modo 1
                iniciarJuego(1);
            }
        });
        Button buttonModo2= findViewById(R.id.buttonModo2);
        buttonModo2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Se lanza actividad con modo 1
                iniciarJuego(2);
            }
        });
    }

    private void iniciarJuego(int modo){
        //Carga las preguntas desde la base de datos en ColeccionPreguntas
        Data datos = new Data.Builder()
                .putString("idioma","0")
                .build();

        OneTimeWorkRequest obtenerPreguntasOtwr= new OneTimeWorkRequest.Builder(obtenerPreguntasWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(obtenerPreguntasOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Si hay alguna pregunta, lanzo la actividad
                        if(workInfo != null && workInfo.getState().isFinished()){
                            if (ColeccionPreguntas.obtenerMiColeccion().obtenerNumeroPreguntas()>0){
                                //Inicia el juego con el modo introducido
                                Intent i = new Intent(getApplicationContext(), GameActivity.class);
                                i.putExtra("modo",modo);
                                startActivity(i);
                            }
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(obtenerPreguntasOtwr);

    }
}
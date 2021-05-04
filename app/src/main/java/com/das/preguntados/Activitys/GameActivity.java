package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.das.preguntados.R;

public class GameActivity extends AppCompatActivity {
    /*ACTIVIDAD QUE GESTIONA EL JUEGO*/
    /*LAS PREGUNTAS YA ESTAN CARGADAS EN LA MAE LISTAPREGUNTAS*/
    boolean isOn=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gestionarJuego();
    }

    private void mostrarTiempoPregunta(int time){
        //Se crea un hilo que actualiza el timeTextView
        final int[] tiempo = {time};
        new Thread() {
            public void run() {
                while (tiempo[0] >= 0 && isOn) { //Si no ha finalizado el tiempo o la variable isOn es true
                    try {
                        int finalTime1 = tiempo[0];
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                ((TextView)findViewById(R.id.timeTextView)).setText(finalTime1 +"''" );
                            }
                        });
                        if (finalTime1==0) break;
                        Thread.sleep(1000);
                        tiempo[0] = tiempo[0] -1;


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Termina el while, por tanto gestiono respuesta si tiempo=0
                if (tiempo[0]==0){
                    gestionarRespuesta(null);
                }
            }
        }.start();

    }
    private void gestionarJuego(){
        mostrarTiempoPregunta(15); //Se inicializa el tiempo para responder la pregunta
        //Se muestran los datos de la pregunta
        //Si se pulsa algun boton o termina el tiempo, llamar al metodo gestionarRespuesta(). Este dira cual es la respuesta correcta, esperara unos segundos para que lo vea y determinara si seguir jugando o terminar
    }

    private void gestionarRespuesta(String respuesta){
        //Recibe como parametro la respuesta del usuario, si es null, se entiende como que se le ha acabado el tiempo
        if (respuesta==null){
            try {Thread.sleep(1000);} catch (Exception e) {}; //Espera x segundos para mostrar la respuesta correcta
            gestionarJuego();
        }
    }
}
package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.das.preguntados.Dialogs.DialogoFinJuego1Fragment;
import com.das.preguntados.Dialogs.DialogoFinJuego2Fragment;
import com.das.preguntados.GameManager.ColeccionPreguntas;
import com.das.preguntados.R;
import com.das.preguntados.WS.obtenerPreguntasWS;
import com.das.preguntados.WS.registrarDatosPartidaWS;

import java.util.Locale;

public class GameSelectorActivity extends AppCompatActivity {
    /*
        ACTIVIDAD PARA ELEGIR EL MODO DE JUEGO A JUGAR
     */
    private String usuario; //Referencia al usuario que ha iniciado sesion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selector);
        //Obtengo el usuario que ha iniciado sesion
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            usuario=extras.getString("usuario");
        }
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
        //Obtengo el idioma de la aplicacion
        String elIdioma = Locale.getDefault().getLanguage();
        String idioma="0"; //Idioma por defecto
        if (elIdioma.equals("en")){ //Si es ingles,
            idioma="1";
        }
        //Carga las preguntas desde la base de datos en ColeccionPreguntas
        Data datos = new Data.Builder()
                .putString("idioma",idioma)
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
                                startActivityForResult(i,100);
                            }
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(obtenerPreguntasOtwr);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Ha finalizado un ActivityForResult, recupero la informacion
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) { //Si ha finalizado correctamente la actividad juego
            int puntuacion = data.getIntExtra("puntuacion",0);
            int preguntasCorrectas = data.getIntExtra("preguntasCorrectas",0);
            int preguntasIncorrectas = data.getIntExtra("preguntasIncorrectas",0);
            int modo= data.getIntExtra("modo",0);
            if (modo==0){
                Log.d("modo","No se ha recibido ningun modo, revisar GameActivity");
            }
            else { //Modo correcto
                //Envio los datos de la partida a Base de datos
                usuario="xabier"; //TODO: Aun no está implementado el enviar el usuario a esta actividad
                Data datos = new Data.Builder()
                        .putString("usuario",usuario)
                        .putInt("modo",modo)
                        .putInt("puntuacion",puntuacion)
                        .putInt("preguntasCorrectas",preguntasCorrectas)
                        .putInt("preguntasIncorrectas",preguntasIncorrectas)
                        .build();
                OneTimeWorkRequest registrarDatosPartidaOtwr= new OneTimeWorkRequest.Builder(registrarDatosPartidaWS.class).setInputData(datos)
                        .build();
                WorkManager.getInstance(getApplicationContext()).enqueue(registrarDatosPartidaOtwr);

                //Muestro el dialog fin de juego
                if (modo==1){ //Dialog Modo de juego 1
                    DialogoFinJuego1Fragment dialogoFinJuego = new DialogoFinJuego1Fragment(preguntasCorrectas);
                    dialogoFinJuego.show(getSupportFragmentManager(),"DialogoFinJuego1");
                }
                else if (modo==2){ //Dialog Modo de juego 2
                    DialogoFinJuego2Fragment dialogoFinJuego = new DialogoFinJuego2Fragment(puntuacion,preguntasCorrectas,preguntasIncorrectas);
                    dialogoFinJuego.show(getSupportFragmentManager(),"DialogoFinJuego2");
                }
            }


        }
    }
}
package com.das.preguntados.Activitys;

import android.content.Intent;
import android.graphics.Color;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.das.preguntados.Common.ActivityVertical;
import com.das.preguntados.Dialogs.DialogoSalirJuegoFragment;
import com.das.preguntados.GameManager.ColeccionPreguntas;
import com.das.preguntados.GameManager.Pregunta;
import com.das.preguntados.R;

public class GameActivity extends ActivityVertical implements DialogoSalirJuegoFragment.ListenerDialogoSalirJuego {
    /*ACTIVIDAD QUE GESTIONA EL JUEGO
    LAS PREGUNTAS YA ESTAN CARGADAS EN LA MAE COLECCIONPREGUNTAS
    Modo 1: Consiste en acertar el mayor número de preguntas posibles
    Modo 2: Consiste en responder preguntas durante 1 minuto*/

    boolean isOn=true;
    Pregunta preguntaActual; //Guarda la pregunta que se está mostrando
    Contador contadorPregunta; //Guarda el contador con el tiempo para responder a la pregunta
    private long tiempoActual;

    private boolean guardarPartida; //Indica si guardar la partida o no
    private boolean abandono=false; //Indica si se ha abandonado la partida o no
    int modo; //Modo de juego

    private int preguntasCorrectas; //Contador preguntas correctas
    private int preguntasIncorrectas; //Contador preguntas incorrectas
    private int racha; //Indica la racha de preguntas correctas consecutivas
    private int puntuacion; //Indica la puntuacion (nunca menor que 0)

    //Variables efectos sonido
    SoundPool sfx;
    int soundAcierto;
    int soundError;
    int soundReloj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //Obtengo el modo de juego
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            modo=extras.getInt("modo");
            guardarPartida=extras.getBoolean("guardarPartida");
            Log.d("gameActivity","Modo de juego= "+modo);
            Log.d("gameActivity","Guardar partida= "+guardarPartida);
        }
        gestionarEventosBotones();
        //Inicializo los datos del juego

        preguntasCorrectas=0;
        preguntasIncorrectas=0;
        puntuacion=0;
        racha=0;

        //Definicion objetos para SFX
        AudioAttributes audioAttributes= new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();
        sfx= new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(audioAttributes).build();
        //Cargo los tres sonidos
        soundAcierto= sfx.load(this,R.raw.acierto,1);
        soundError= sfx.load(this,R.raw.error,1);
        soundReloj = sfx.load(this,R.raw.reloj,1);

        if (modo==1){
            gestionarJuegoModo1();
        }
        if (modo==2){
            gestionarInicioJuegoModo2();
        }
    }

    private void gestionarJuegoModo1(){
        //Metodo principal encargado de gestionar el juego con el modo 1
        //Carga una pregunta que se muestra durante un tiempo
        isOn=true; //Inicio el juego
        //Se inicializa el tiempo para responder la pregunta (15 segundos)
        contadorPregunta= new Contador(15*1000,1000);
        contadorPregunta.start();
        //Muestro los datos de la pregunta
        cargarPregunta();
    }
    private void gestionarInicioJuegoModo2(){
        //Metodo principal encargado de gestionar el inicio del juego con el modo 2

        //Se inicializa el tiempo para responder las preguntas(100 segundos)
        tiempoActual=60*1000; //Tiempo inicial 60 segundos
        gestionarJuegoModo2();
    }
    private void gestionarJuegoModo2(){
        //Carga preguntas durante un periodo de tiempo
        isOn=true; //Inicio el juego
        contadorPregunta= new Contador(tiempoActual,250); //Simula un resume del contador, intervalo menor para guardar el tiempo mas exacto
        contadorPregunta.start();
        cargarPregunta();
    }

    private void cargarPregunta(){
        //Carga una pregunta en la actividad
        preguntaActual= ColeccionPreguntas.obtenerMiColeccion().obtenerPreguntaAlAzar();
        if (preguntaActual!=null){
            //Cargo los datos de la pregunta en los distintos elementos del layout
            ((TextView)findViewById(R.id.rachaTextView)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.resultadoPreguntaView)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.categoryTextView)).setText(preguntaActual.getGeneroPregunta());
            ((TextView)findViewById(R.id.preguntaTextView)).setText(preguntaActual.getTextoPregunta());
            Button botonA=findViewById(R.id.buttonRespuestaA);
            botonA.setBackground(getDrawable(R.drawable.boton_redondeado_gris));
            botonA.setText(preguntaActual.getTextoOpcionA());
            Button botonB=findViewById(R.id.buttonRespuestaB);
            botonB.setBackground(getDrawable(R.drawable.boton_redondeado_gris));
            botonB.setText(preguntaActual.getTextoOpcionB());
            Button botonC=findViewById(R.id.buttonRespuestaC);
            botonC.setBackground(getDrawable(R.drawable.boton_redondeado_gris));
            botonC.setText(preguntaActual.getTextoOpcionC());
        }
        else{ //No quedan preguntas, se finaliza el juego
            Log.d("cargarPregunta","Se han acabado las preguntas");
            //Muestro un toast indicando que no quedan preguntas
            Toast.makeText(this,getString(R.string.toast_noPreguntas),Toast.LENGTH_LONG).show();
            contadorPregunta.cancel(); //Cancelo el contador
            finish(); //Finalizo la actividad
        }
    }

    private void gestionarRespuesta(String respuesta){
        //Recibe como parametro la respuesta del usuario. Se encarga de pintar si es respuesta correcta o no, y decidir si continuar el juego o finalizarlo
        if (isOn) { //Si no está el juego parado (evitar volver a pulsar el boton dos veces)
            isOn = false; //Paro el juego
            pintarBoton(preguntaActual.getOpcionGanadora(), "green"); //Pinto la respuesta correcta
            boolean continuar=false;

            if (respuesta == null) { //Si no hay respuesta, es que se ha acabado el tiempo
                if (modo==1){ //Si es el modo de juego 1, se da como incorrecta
                    preguntasIncorrectas++;
                    sfx.play(soundError,1,1,1,0,1);
                }
                pintarBoton(preguntaActual.getOpcionGanadora(), "green");
                TextView resultado= findViewById(R.id.resultadoPreguntaView);
                resultado.setText(getString(R.string.game_finTiempo));
                resultado.setTextColor(Color.MAGENTA);
                resultado.setVisibility(View.VISIBLE);

            }
            else { //Si hay respuesta, compruebo si es correcta o no
                if (respuesta.equals(preguntaActual.getOpcionGanadora())) { //Respuesta correcta
                    preguntasCorrectas++;
                    continuar=true;
                    sfx.play(soundAcierto,1,1,1,0,1);
                    TextView resultado= findViewById(R.id.resultadoPreguntaView);
                    resultado.setText(getString(R.string.game_resultadoCorrecto));
                    resultado.setTextColor(Color.GREEN);
                    resultado.setVisibility(View.VISIBLE);
                    if (modo==2){ //Gestiona la puntuacion para el modo 2
                        racha++; //Aumento la racha
                        puntuacion=puntuacion+(10*racha);
                    }
                } else { //Respuesta incorrecta
                    //Pinto de rojo el mio, pinto de verde el bueno
                    preguntasIncorrectas++;
                    pintarBoton(respuesta, "red");
                    //Dependiendo del modo de juego, resto puntuacion
                    TextView resultado= findViewById(R.id.resultadoPreguntaView);
                    resultado.setText(getString(R.string.game_resultadoIncorrecto));
                    resultado.setTextColor(Color.RED);
                    resultado.setVisibility(View.VISIBLE);
                    sfx.play(soundError,1,1,1,0,1);
                    //Si el modo de juego es 2, no termina el juego
                    if (modo==2){
                        continuar=true;
                        //Gestion de la puntuacion
                        racha=0; //Reseteo la racha
                        //Resto 10 a la puntuacion si es posible
                        if (puntuacion>=10){
                            puntuacion=puntuacion-10;
                        }
                        else{ //Si es menor que 10, directamente pongo 0
                            puntuacion=0;
                        }
                    }
                }
            }
            actualizarPuntuacion();
            gestionarFinDelJuego(continuar);
        }

    }
    private void gestionarFinDelJuego(boolean continuar){
        //Recibe como parametro si se ha acertado la pregunta o no. Gestiona si carga una nueva pregunta o finaliza la actividad
        if (continuar){
            //Muestro la respuesta durante 3 segundos y cargo una nueva pregunta
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (modo==1){
                        gestionarJuegoModo1();
                    }
                    if (modo==2){
                        gestionarJuegoModo2();
                    }
                }
             },3000);
        }
        else{
            //Muestro la respuesta durante 3 segundos y finalizo la actividad
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    contadorPregunta.cancel();
                    finish();
                }
            },3000);
        }

    }

    private void gestionarEventosBotones(){
        //Gestiona los eventos de los botones Opcion A, B y C

        //Evento opcion A
        Button buttonA= findViewById(R.id.buttonRespuestaA);
        buttonA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                contadorPregunta.cancel();
                gestionarRespuesta("A");
            }
        });
        Button buttonB= findViewById(R.id.buttonRespuestaB);
        buttonB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                contadorPregunta.cancel();
                gestionarRespuesta("B");
            }
        });
        Button buttonC= findViewById(R.id.buttonRespuestaC);
        buttonC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                contadorPregunta.cancel();
                gestionarRespuesta("C");
            }
        });
    }

    private void pintarBoton(String boton, String color){
        //Recibe como parametros el identificador del boton (A,B o C) y el color a pintar ("green","red" o "gray")
        if (boton.equals("A")){
            Button buttonA= findViewById(R.id.buttonRespuestaA);
            if (color.equals("green")){
                buttonA.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_acierto));
            }
            else if (color.equals("red")){
                buttonA.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_fallo));
            }
            else if (color.equals("gray")){
                buttonA.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_gris));
            }
            else{
                Log.d("pintarBoton","La referencia al color enviada no es correcta");
            }
        }
        else if (boton.equals("B")){
            Button buttonB= findViewById(R.id.buttonRespuestaB);
            if (color.equals("green")){
                buttonB.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_acierto));
            }
            else if (color.equals("red")){
                buttonB.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_fallo));
            }
            else if (color.equals("gray")){
                buttonB.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_gris));
            }
            else{
                Log.d("pintarBoton","La referencia al color enviada no es correcta");
            }
        }
        else if (boton.equals("C")){
            Button buttonC= findViewById(R.id.buttonRespuestaC);
            if (color.equals("green")){
                buttonC.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_acierto));
            }
            else if (color.equals("red")){
                buttonC.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_fallo));
            }
            else if (color.equals("gray")){
                buttonC.setBackground(getApplicationContext().getDrawable(R.drawable.boton_redondeado_gris));
            }
            else{
                Log.d("pintarBoton","La referencia al color enviada no es correcta");
            }
        }
        else{
            Log.d("pintarBoton","La referencia al boton enviada no es correcta");
        }
    }

    private void actualizarPuntuacion(){
        //Actualiza el layout de la puntuacion
        String puntuacionLayout="0";
        if (modo==1){ //Si es el primer modo de juego, puntuacion = numero respuestas correctas
            puntuacionLayout=String.valueOf(preguntasCorrectas);
        }
        if (modo==2){
            puntuacionLayout=String.valueOf(puntuacion);
        }
        ((TextView)findViewById(R.id.puntuacionTextView)).setText(puntuacionLayout);
        //Si llevas una racha, muestras al lado del layout durante el tiempo que esperas
        if (racha>1){
            TextView rachaTextView= findViewById(R.id.rachaTextView);
            rachaTextView.setText("x"+racha);
            rachaTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onBackPressed(){
        DialogoSalirJuegoFragment dialogoSalirJuego = new DialogoSalirJuegoFragment();
        dialogoSalirJuego.show(getSupportFragmentManager(), "DialogoSalirJuego");
    }

    public void abandonarJuego(){
        //Metodo para abandonar la partida
        guardarPartida=false; //Indico que no se guarda la partida
        abandono=true; //Indico que he abandonado la partida
        contadorPregunta.cancel(); //Cancelo el contador
        finish(); //Finalizo la actividad
    }
    public void finish(){
        //Reimplementacion del metodo finish para enviar los resultados del juego
        Intent intent=new Intent();
        //Devuelvo los resultados del juego y el modo
        intent.putExtra("puntuacion", puntuacion);
        intent.putExtra("preguntasCorrectas", preguntasCorrectas);
        intent.putExtra("preguntasIncorrectas", preguntasIncorrectas);
        intent.putExtra("modo",modo);
        intent.putExtra("guardarPartida",guardarPartida);
        intent.putExtra("abandono",abandono);
        setResult(RESULT_OK, intent);
        super.finish();
    }


    private class Contador extends CountDownTimer {
        //CLASE PARA IMPLEMENTAR EL CONTADOR DE LAS PREGUNTAS. Recibe como parametros tiempo inicial y tiempo de tick, actualizando el timeTextView cada tick
        private long segundo=0;
        public Contador(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            ((TextView)findViewById(R.id.timeTextView)).setText(l / 1000 +"''" );
            tiempoActual=l; //Guardamos el tiempo actual para recuperarlo

            if (l/1000<=5 && segundo!=l/1000){ //Si quedan menos de 5 segundos, reproduzco efecto de sonido
                segundo=l/1000;
                sfx.play(3,1,1,1,0,1);
            }
        }

        @Override
        public void onFinish() {
            //Se ha acabado el tiempo, no ha respondido nada
            gestionarRespuesta(null);
        }
    }

}

package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;

import com.das.preguntados.GameManager.ColeccionPreguntas;
import com.das.preguntados.GameManager.Pregunta;
import com.das.preguntados.R;

public class GameActivity extends AppCompatActivity {
    /*ACTIVIDAD QUE GESTIONA EL JUEGO
    LAS PREGUNTAS YA ESTAN CARGADAS EN LA MAE COLECCIONPREGUNTAS
    Modo 1: Consiste en acertar el mayor número de preguntas posibles
    Modo 2: Consiste en responder preguntas durante 1 minuto
    TODO: Hacer algun metodo para que cuando pulse atras avise que se finalizará el juego y no se guardarán estadisticas*/

    boolean isOn=true;
    Pregunta preguntaActual; //Guarda la pregunta que se está mostrando
    Contador contadorPregunta; //Guarda el contador con el tiempo para responder a la pregunta
    private long tiempoActual;
    int modo; //Modo de juego

    private int preguntasCorrectas;
    private int preguntasIncorrectas;

    private int racha; //Indica la racha de preguntas correctas consecutivas
    private int puntuacion; //Indica la puntuacion (nunca menor que 0)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //Obtengo el modo de juego
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            modo=extras.getInt("modo");
            Log.d("gameActivity","Modo de juego= "+modo);
        }
        gestionarEventosBotones();
        //Inicializo los datos del juego
        preguntasCorrectas=0;
        preguntasIncorrectas=0;
        puntuacion=0;
        racha=0;
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
        tiempoActual=60*1000; //Tiempo inicial 100 segundos
        gestionarJuegoModo2();
    }
    private void gestionarJuegoModo2(){
        //Carga preguntas durante un periodo de tiempo
        isOn=true; //Inicio el juego
        contadorPregunta= new Contador(tiempoActual,1000);
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
        else{
            Log.d("cargarPregunta","Se han acabado las preguntas");
            /*TODO: Mostrar una alerta o algo indicando que no quedan preguntas */
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
                preguntasIncorrectas++;
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
    public void finish(){
        //Reimplementacion del metodo finish para enviar los resultados del juego
        Intent intent=new Intent();
        //Devuelvo los resultados del juego y el modo
        intent.putExtra("puntuacion", puntuacion);
        intent.putExtra("preguntasCorrectas", preguntasCorrectas);
        intent.putExtra("preguntasIncorrectas", preguntasIncorrectas);
        intent.putExtra("modo",modo);
        setResult(RESULT_OK, intent);
        super.finish();
    }


    private class Contador extends CountDownTimer {
        //CLASE PARA IMPLEMENTAR EL CONTADOR DE LAS PREGUNTAS. Recibe como parametros tiempo inicial y tiempo de tick, actualizando el timeTextView cada tick
        public Contador(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            ((TextView)findViewById(R.id.timeTextView)).setText(l / 1000 +"''" );
            tiempoActual=l; //Guardamos el tiempo actual para recuperarlo
        }

        @Override
        public void onFinish() {
            //Se ha acabado el tiempo, no ha respondido nada
            gestionarRespuesta(null);
        }
    }

}

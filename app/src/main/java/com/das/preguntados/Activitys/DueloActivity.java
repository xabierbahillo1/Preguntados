package com.das.preguntados.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DueloActivity extends ActivityVertical implements DialogoSalirJuegoFragment.ListenerDialogoSalirJuego {
    //Actividad que gestiona el duelo entre dos jugadores HOST y GUEST

    private String usuario; //Referencia al usuario que ha iniciado sesion
    private String roomName; //Nombre de la sala
    private String role; //Rol en la sala

    //Atributos de la base de datos
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://preguntados-2f25f-default-rtdb.europe-west1.firebasedatabase.app/"); //Instancia a base de datos
    private DatabaseReference roomRef;
    private ValueEventListener listener;

    private String nombreGuest; //Nombre del usuario que se une a la partida
    private boolean esperandoGuest=false; //Indica si se esta esperando al guest
    private ProgressDialog progressDialog; //Dialogo esperando a guest

    //Variables resultado partida
    private int aciertosHost=0;
    private int aciertosGuest=0;

    private boolean isOn;

    Pregunta preguntaActual; //Guarda la pregunta que se está mostrando

    String textoPreguntaActual=""; //Guarda la pregunta actual en formato texto (para el guest)

    Contador contadorPregunta; //Guarda el contador con el tiempo para responder a la pregunta

    private String turno;

    //Booleanos para gestionar los abandonos
    private boolean abandonoYo = false;
    private boolean abandonaEl = false;

    //Variables efectos sonido
    SoundPool sfx;
    int soundAcierto;
    int soundError;
    int soundReloj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duelo);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usuario = extras.getString("usuario");
            roomName = extras.getString("roomName");
            //Obtengo el rol
            if (usuario.equals(roomName)) {
                role = "Host";
                esperandoGuest=true;
            } else {
                role = "Guest";
                esperandoGuest=false;
            }
        }

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

        //Si es host Oculto campos que no quiero que se vean en un primer momento
        if (role.equals("Host")) {
            findViewById(R.id.resultadoPreguntaView).setVisibility(View.GONE);
            findViewById(R.id.preguntaTextView).setVisibility(View.GONE);
            findViewById(R.id.timeTextView).setVisibility(View.GONE);
            findViewById(R.id.categoryTextView).setVisibility(View.GONE);
            findViewById(R.id.turnoText).setVisibility(View.GONE);
            //Si es host, muestro el progressDialog hasta que aparezca un guest
            progressDialog = new ProgressDialog(this);
            progressDialog.setIcon(R.mipmap.ic_launcher);
            progressDialog.setMessage(getString(R.string.duelo_waitOponent));
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    Log.i("esperandoOponente", "Cancelado mientras espero al oponente");
                    finish(); //Finalizo la actividad
                }
            });
            progressDialog.show();
        }

        if (role.equals("Guest")){
            //Si es guest
            aciertosHost=0;
            aciertosGuest=0;
        }

        turno=""; //Empieza jugando el host
        gestionarEventosBotones();
        roomRef= database.getReference("salas/"+roomName);
        setDatabaseListener();

    }

    private void setDatabaseListener() {
        //Metodo para gestionar los datos recibidos en base de datos
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Te unes a la sala roomName
                //Si vas a ser el host de la partida, se cargan las preguntas, si no, se lanza directamente la actividad
                Iterable<DataSnapshot> data= dataSnapshot.getChildren();
                for(DataSnapshot snapshot: data) { //Compruebo los datos en BD
                    if (esperandoGuest && snapshot.getKey().equals("guest")){ //Si estoy esperando al guest y se conecta
                        nombreGuest=snapshot.getValue(String.class);
                        esperandoGuest=false;
                        progressDialog.dismiss();
                        comenzarJuego();
                    }
                    if (role.equals("Guest") && snapshot.getKey().equals("pregunta")){ //Si eres guest y es una pregunta
                        String laPregunta= snapshot.getValue(String.class);
                        //Compruebo que sea diferente a la de antes
                        if (!textoPreguntaActual.equals(laPregunta)){
                            textoPreguntaActual=laPregunta;
                            String[] datosPregunta=laPregunta.split(";"); //La pregunta viene separada en ;
                            preguntaActual= new Pregunta(datosPregunta[0],datosPregunta[1],datosPregunta[2],datosPregunta[3],datosPregunta[4],datosPregunta[5]);
                            cargarPregunta();
                        }
                    }
                    if ((!role.equals(turno)) && snapshot.getKey().equals("respuesta")){ //Si no es tu turno y recibes una respuesta
                        contadorPregunta.cancel();
                        String laRespuesta= snapshot.getValue(String.class);
                        //Compruebo que sea diferente a la de antes
                        gestionarRespuesta(laRespuesta);
                    }
                    if (snapshot.getKey().equals("finish")){ //Fin del juego
                        finish();
                    }
                    if (snapshot.getKey().equals("abandonar")){ //Alguien ha abandonado
                        if (!abandonoYo){ //Si no he abandonado yo, ha sido el otro jugador
                            abandonaEl=true;
                        }
                        contadorPregunta.cancel();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("unirSalas", "Error al unirte a la sala");
            }
        };

        roomRef.addValueEventListener(listener);
    }

    private void comenzarJuego(){
        //Comienza el juego, para ello, pongo visibles todas los elementos que puse invisibles
        findViewById(R.id.preguntaTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.timeTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.categoryTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.turnoText).setVisibility(View.VISIBLE);
        aciertosHost=0;
        aciertosGuest=0;

        cargarPregunta();
    }

    private void cargarPregunta(){
        //Se inicializa el tiempo para responder la pregunta (15 segundos)
        isOn=true; //Inicio el juego

        contadorPregunta= new Contador(15*1000,1000);
        contadorPregunta.start();

        gestionarTurno();

        if (role.equals("Host")){ //Si es host, obtengo la pregunta desde la clase
            preguntaActual= ColeccionPreguntas.obtenerMiColeccion().obtenerPreguntaAlAzar();
            //Subo la pregunta a la base de datos
            if (preguntaActual!=null){ //Subo la pregunta
                String preguntaBD=preguntaActual.generarStringPregunta();
                roomRef.child("pregunta").setValue(preguntaBD); //Guardo la pregunta en la base de datos
            }
            else{ //No quedan preguntas, se finaliza el juego
                Log.d("cargarPregunta","Se han acabado las preguntas");
                //Muestro un toast indicando que no quedan preguntas
                Toast.makeText(this,getString(R.string.toast_noPreguntas),Toast.LENGTH_LONG).show();
                contadorPregunta.cancel(); //Cancelo el contador
                roomRef.child("finish").setValue("true"); //Informo de que se finaliza la partida
                finish(); //Finalizo la actividad
            }
        }
        //Si es guest, ya deberia tener la pregunta
        if (preguntaActual!=null){
            //Cargo los datos de la pregunta en los distintos elementos del layout
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
    }
    private void gestionarTurno(){
        //Gestiona el turno actual
        //Gestion del turno
        if (turno.equals("")){ //Primera ejecucion, empieza el host
            turno="Host";
        }
        else if (turno.equals("Host")){ //El turno anterior jugo Host
            turno="Guest";
        }
        else if (turno.equals("Guest")){
            turno="Host";
        }
        //Muestro el turno
        if (turno.equals(role)){ //Es tu turno
            ((TextView)findViewById(R.id.turnoText)).setText(getString(R.string.duelo_miturno));
            //Vibra para avisar que es tu turno
            Vibrator vibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500); //Vibra medio segundo
        }
        else{ //turno del contrario
            if (role.equals("Host")){
                ((TextView)findViewById(R.id.turnoText)).setText(getString(R.string.duelo_turnoDe)+" "+nombreGuest);
            }
            else{
                ((TextView)findViewById(R.id.turnoText)).setText(getString(R.string.duelo_turnoDe)+" "+roomName);
            }

        }

        //Limpio la respuesta del turno anterior
        roomRef.child("respuesta").removeValue();
    }

    private void gestionarEventosBotones(){
        //Gestiona los eventos de los botones Opcion A, B y C

        //Evento opcion A
        Button buttonA= findViewById(R.id.buttonRespuestaA);
        buttonA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (turno.equals(role)){
                    contadorPregunta.cancel();
                    gestionarRespuesta("A");
                }


            }
        });
        Button buttonB= findViewById(R.id.buttonRespuestaB);
        buttonB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (turno.equals(role)){
                    contadorPregunta.cancel();
                    gestionarRespuesta("B");
                }

            }
        });
        Button buttonC= findViewById(R.id.buttonRespuestaC);
        buttonC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (turno.equals(role)){
                    contadorPregunta.cancel();
                    gestionarRespuesta("C");
                }

            }
        });
    }

    private void gestionarRespuesta(String respuesta) {
        //Recibe como parametro la respuesta del usuario. Se encarga de pintar si es respuesta correcta o no, y decidir si continuar el juego o finalizarlo
        if (isOn) { //Si no está el juego parado (evitar volver a pulsar el boton dos veces)

            if (role.equals(turno)){ //Envio la respuesta a base de datos
                roomRef.child("respuesta").setValue(respuesta); //Guardo la pregunta en la base de datos
            }

            isOn = false; //Paro el juego
            pintarBoton(preguntaActual.getOpcionGanadora(), "green"); //Pinto la respuesta correcta
            boolean continuar = false;


            if (respuesta.equals("timeOut")) { //Si se ha acabado el tiempo
                pintarBoton(preguntaActual.getOpcionGanadora(), "green");
                TextView resultado = findViewById(R.id.resultadoPreguntaView);
                resultado.setText(getString(R.string.game_finTiempo));
                resultado.setTextColor(Color.MAGENTA);
                resultado.setVisibility(View.VISIBLE);

                sfx.play(soundError,1,1,1,0,1);
            } else { //Si hay respuesta, compruebo si es correcta o no
                if (respuesta.equals(preguntaActual.getOpcionGanadora())) { //Respuesta correcta
                    //Sumo 1 al turno correspondiente
                    if (turno.equals("Host")){
                        aciertosHost++;
                    }
                    else if (turno.equals("Guest")){
                        aciertosGuest++;
                    }
                    continuar = true;
                    TextView resultado = findViewById(R.id.resultadoPreguntaView);
                    resultado.setText(getString(R.string.game_resultadoCorrecto));
                    resultado.setTextColor(Color.GREEN);
                    resultado.setVisibility(View.VISIBLE);
                    sfx.play(soundAcierto,1,1,1,0,1);
                } else { //Respuesta incorrecta
                    //Pinto de rojo el mio, pinto de verde el bueno
                    pintarBoton(respuesta, "red");
                    //Dependiendo del modo de juego, resto puntuacion
                    TextView resultado = findViewById(R.id.resultadoPreguntaView);
                    resultado.setText(getString(R.string.game_resultadoIncorrecto));
                    resultado.setTextColor(Color.RED);
                    resultado.setVisibility(View.VISIBLE);
                    sfx.play(soundError,1,1,1,0,1);
                }
            }
            actualizarPuntuacion();

            if (role.equals("Host")){
                gestionarFinDelJuego();
            }

        }
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
        String puntuacionLayout=String.valueOf(aciertosHost)+"/"+String.valueOf(aciertosGuest);
        ((TextView)findViewById(R.id.puntuacionTextView)).setText(puntuacionLayout);
    }
    private void gestionarFinDelJuego(){
        //Si algun jugador llega a 6 aciertos, termino el juego
        if (aciertosHost==6 || aciertosGuest == 6){ //Fin del juego
            //Muestro la respuesta durante 3 segundos y finalizo el juego
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Comunico el finish
                    roomRef.child("finish").setValue("true");
                    finish();
                }
            },3000);
        }
        else{
            //Muestro la respuesta durante 3 segundos y cargo una nueva pregunta
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cargarPregunta();
                }
            },3000);
        }
    }

    public void finish() {
        //Reimplementacion del metodo finish
        Intent intent=new Intent();
        //Devuelvo los resultados del juego
        intent.putExtra("esperandoGuest", esperandoGuest);
        if (!esperandoGuest){ //Si ha empezado la partida, envio el resto de datos
            intent.putExtra("guest",nombreGuest);
            intent.putExtra("abandonoYo",abandonoYo);
            intent.putExtra("abandonaEl",abandonaEl);
            intent.putExtra("aciertosHost",aciertosHost);
            intent.putExtra("aciertosGuest",aciertosGuest);
            intent.putExtra("roomName",roomName);
            intent.putExtra("role",role);
        }

        if (role.equals("Host")) {
            //Si es host, destruyo la sala y finalizo la actividad
            DatabaseReference eliminar = database.getReference("salas");
            eliminar.child(roomName).removeValue(); //Limpio la sala xabier
        }
        roomRef.removeEventListener(listener);//Dejo de escuchar
        setResult(RESULT_OK, intent);
        super.finish();
    }

    public void onBackPressed(){
        DialogoSalirJuegoFragment dialogoSalirJuego = new DialogoSalirJuegoFragment();
        dialogoSalirJuego.show(getSupportFragmentManager(), "DialogoSalirJuego");
    }

    @Override
    public void abandonarJuego() {
        //Marco abandono y se gestiona desde la propia bd
        roomRef.child("abandonar").setValue("true");
        abandonoYo=true;
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
            if (l/1000<=5 && segundo!=l/1000){ //Si quedan menos de 5 segundos, reproduzco efecto de sonido
                segundo=l/1000;
                sfx.play(3,1,1,1,0,1);
            }
        }

        @Override
        public void onFinish() {
            //Se ha acabado el tiempo, no ha respondido nada
            if (role.equals(turno)){
                gestionarRespuesta("timeOut");
            }

        }
    }

}
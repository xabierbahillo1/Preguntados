package com.das.preguntados.Activitys;

import androidx.annotation.NonNull;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.das.preguntados.Common.ActivityVertical;
import com.das.preguntados.Dialogs.DialogMessage;
import com.das.preguntados.Dialogs.DialogoFinJuego1Fragment;
import com.das.preguntados.Dialogs.DialogoFinJuego2Fragment;
import com.das.preguntados.GameManager.ColeccionPreguntas;
import com.das.preguntados.R;
import com.das.preguntados.WS.obtenerPreguntasWS;
import com.das.preguntados.WS.registrarDatosDueloWS;
import com.das.preguntados.WS.registrarDatosPartidaWS;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class RoomActivity extends ActivityVertical {
    //Actividad para mostrar y crear salas de duelo

    private String usuario; //Referencia al usuario que ha iniciado sesion
    private List<String> roomList; //Lista de salas disponibles
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://preguntados-2f25f-default-rtdb.europe-west1.firebasedatabase.app/"); //Instancia a base de datos
    private DatabaseReference roomRef;
    private ListView listaSalas;
    private String roomName; //Nombre de la sala
    private String genero; //Opciones de preguntas

    private ValueEventListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        //Obtengo el usuario que ha iniciado sesion
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            usuario=extras.getString("usuario");
            genero=extras.getString("opcionesString");
        }

        listaSalas=findViewById(R.id.listRooms);
        roomList = new ArrayList<>();

        Button botonCrearSala= findViewById(R.id.buttonCrearSala);
        botonCrearSala.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Se crea una sala
                crearSala();
            }
        });

        listaSalas.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Obtengo la referencia de la sala y añado al jugador como invitado
                roomName= roomList.get(i); //Obtengo el nombre de la sala
                roomRef= database.getReference("salas/"+roomName+"/guest");
                addRoomEventListener();
                roomRef.setValue(usuario);
            }
        });
        getRooms();
    }

    private void crearSala() {
        //Crea una sala y añade al jugador como host
        roomName= usuario; //El nombre de la sala es el usuario
        roomRef = database.getReference("salas/"+roomName+"/host");
        addRoomEventListener();
        roomRef.setValue(usuario);


    }
    private void addRoomEventListener() {
        //Metodo para gestionar la accion sobre una sala
        listener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Te unes a la sala roomName
                //Si vas a ser el host de la partida, se cargan las preguntas, si no, se lanza directamente la actividad
                if (roomName.equals(usuario)) { //Host, cargo las preguntas y lanzo actividad
                    cargarPreguntas();
                } else { //Guest, se lanza la actividad
                    Intent i = new Intent(getApplicationContext(), DueloActivity.class);
                    i.putExtra("roomName", roomName);
                    i.putExtra("usuario", usuario);
                    startActivityForResult(i,101);
                }
                //Borro el eventListener
                roomRef.removeEventListener(listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("unirSalas","Error al unirte a la sala");
            }
        };

        roomRef.addValueEventListener(listener);
    }

    private void getRooms(){
        //Obtiene las salas en la base de datos
        DatabaseReference roomsRef= database.getReference("salas");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear(); //Limpio la lista
                Iterable<DataSnapshot> rooms= dataSnapshot.getChildren();
                for(DataSnapshot snapshot: rooms){
                    //Compruebo los child de cada una de las claves para ver si ya está llena la sala o no
                    roomsRef.child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> values= dataSnapshot.getChildren();
                            boolean containsGuest=false;
                            //Compruebo si contiene un guest
                            for (DataSnapshot snapshot1: values){
                                if (snapshot1.getKey().equals("guest")){
                                    containsGuest=true;
                                }
                            }
                            //Si no contiene guest, añado la sala
                            if (!containsGuest) {
                                if (!snapshot.getKey().equals(usuario)) { //Evito añadirme a mi mismo
                                    roomList.add(snapshot.getKey());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(RoomActivity.this, android.R.layout.simple_list_item_1, roomList);
                                listaSalas.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                if (roomList.size() == 0) { //Si no hay salas muestro mensaje
                                    findViewById(R.id.textViewNoSalas).setVisibility(View.VISIBLE);
                                } else { //Oculto mensaje
                                    findViewById(R.id.textViewNoSalas).setVisibility(View.GONE);
                                }
                            }
                            //Actualizo ListView
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(RoomActivity.this, android.R.layout.simple_list_item_1, roomList);
                            listaSalas.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            if (roomList.size() == 0) { //Si no hay salas muestro mensaje
                                findViewById(R.id.textViewNoSalas).setVisibility(View.VISIBLE);
                            } else { //Oculto mensaje
                                findViewById(R.id.textViewNoSalas).setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("obtenerSalas","Error al comprobar si hay guest en las salas");
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("obtenerSalas","Error al obtener las salas");
            }
        });
    }

    private void cargarPreguntas(){
        String elIdioma = Locale.getDefault().getLanguage();
        String idioma="0"; //Idioma por defecto
        if (elIdioma.equals("en")){ //Si es ingles,
            idioma="1";
        }
        //Carga las preguntas desde la base de datos en ColeccionPreguntas
        Data datos = new Data.Builder()
                .putString("idioma",idioma)
                .putString("genero",genero)
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
                                //Se lanza la actividad
                                Intent i = new Intent(getApplicationContext(),DueloActivity.class);
                                i.putExtra("roomName",roomName);
                                i.putExtra("usuario",usuario);
                                startActivityForResult(i,102);
                            }
                            else{ //No se han podido cargar las preguntas, destruyo la sala
                                DatabaseReference eliminar= database.getReference("salas");
                                eliminar.child(roomName).removeValue(); //Limpio la sala xabier
                                Log.d("errorCarga","No se han podido cargar las preguntas");
                                //Lanzo un dialog para informar
                                DialogFragment dialogoError= DialogMessage.newInstance(getString(R.string.salas_alertTitle),getString(R.string.salas_noPreguntas));
                                dialogoError.show(getSupportFragmentManager(), "errorSalas");
                            }
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(obtenerPreguntasOtwr);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Ha finalizado un ActivityForResult, recupero la informacion
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == 101 || requestCode ==102)) { //Si ha finalizado correctamente la actividad Duelo (identificadores 101 o 102)
            boolean esperandoGuest = data.getBooleanExtra("esperandoGuest",true);
            if (!esperandoGuest){ //Si no estaba esperando al guest (ha comenzado la partida)
                String guest=data.getStringExtra("guest");
                boolean abandonoYo=data.getBooleanExtra("abandonoYo",false);
                boolean abandonaEl=data.getBooleanExtra("abandonaEl",false);
                int aciertosHost= data.getIntExtra("aciertosHost",0);
                int aciertosGuest= data.getIntExtra("aciertosGuest",0);
                String roomName= data.getStringExtra("roomName");
                String role= data.getStringExtra("role");
                String ganador="";
                //Obtengo el nombre del otro jugador
                String el="";
                if (role.equals("Host")){
                    el=guest;
                }
                else{
                    el=roomName;
                }
                //Compruebo abandonos
                if (abandonoYo){
                    ganador=guest;
                    lanzarMensajeFinJuego(getString(R.string.duelo_derrotaTitle),getString(R.string.duelo_abandonoYo));
                }
                else if (abandonaEl){
                    ganador=usuario;
                    lanzarMensajeFinJuego(getString(R.string.duelo_victoriaTitle),getString(R.string.duelo_abandonaEl1)+" "+el+" "+getString(R.string.duelo_abandonaEl2));
                }
                else{ //La partida ha finalizo, obtengo ganador y envio los datos a la base de datos
                    //El ganador es quien mas aciertos tiene
                    if (aciertosHost>aciertosGuest){ //Gana host
                        if (role.equals("Host")){ //Gano yo
                            ganador=usuario;
                        }
                        else{ //Gana el
                            ganador=roomName;
                        }
                    }
                    else if (aciertosHost==aciertosGuest){ //Empate
                        ganador="Empate";
                    }
                    else{ //Gana guest
                        if (role.equals("Guest")){ //Gano yo
                            ganador=usuario;
                        }
                        else{ //Gana el
                            ganador=guest;
                        }
                    }

                    if (ganador.equals(usuario)){ //He ganado
                        lanzarMensajeFinJuego(getString(R.string.duelo_victoriaTitle),getString(R.string.duelo_victoria)+" "+el);
                    }
                    else if (ganador.equals("Empate")){ //Empate
                        lanzarMensajeFinJuego(getString(R.string.duelo_empateTitle),getString(R.string.duelo_empateText)+" "+el);
                    }
                    else{ //He perdido
                        lanzarMensajeFinJuego(getString(R.string.duelo_derrotaTitle),getString(R.string.duelo_derrota1)+" "+el+" "+getString(R.string.duelo_derrota2));
                    }

                    //El host envia los datos a la base de datos
                    if (role.equals("Host")){
                        Data datos = new Data.Builder()
                                .putString("host", usuario) //El host soy yo
                                .putString("guest", guest)
                                .putInt("aciertosHost", aciertosHost)
                                .putInt("aciertosGuest", aciertosGuest)
                                .putString("ganador", ganador)
                                .build();
                        OneTimeWorkRequest registrarDatosDueloOtwr = new OneTimeWorkRequest.Builder(registrarDatosDueloWS.class).setInputData(datos)
                                .build();
                        WorkManager.getInstance(getApplicationContext()).enqueue(registrarDatosDueloOtwr);
                    }
                }
            }
        }
    }

    private void lanzarMensajeFinJuego(String titulo, String mensaje){
        //Lanza la actividad que muestra el mensaje fin del juego

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(getString(R.string.alert_btnContinuar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Si dice que sí, se recrea la actividad, para evitar que se quede una sala vieja
                roomRef.removeEventListener(listener); //Borro el eventListener
                recreate();
            }
        });
        builder.show();
    }
}


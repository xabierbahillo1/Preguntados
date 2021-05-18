package com.das.preguntados.Activitys;

import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.das.preguntados.Common.ActivityVertical;
import com.das.preguntados.Common.generateAuthToken;
import com.das.preguntados.R;
import com.das.preguntados.WS.obtenerDatosUsuarioWS;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProfileActivity extends ActivityVertical {

    private String usuario;
    private String nombre;
    private String email;
    private String foto; //Foto en formato base-64
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (android.os.Build.VERSION.SDK_INT > 9) { //Permito descargas en primer plano
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            usuario=extras.getString("usuario");
        }

        TextView textUsuario = findViewById(R.id.textUsuario);
        textUsuario.setText("@"+usuario);

        //Se ocultan todos los campos hasta cargar todos los datos
        visibilidadCampos(View.GONE);

        obtenerDatos(usuario);

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setMessage(R.string.textCerrarSesion).setPositiveButton(R.string.textSi, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Procedimiento cerrar sesion
                        cerrarSesion();
                    }
                }).setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // No hacer nada, el usuario ha cancelado el Dialog
                    }
                });
                builder.show();

            }
        });

        Button btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent i = new Intent(getApplicationContext(), EditProfileActivity.class);
                i.putExtra("foto", foto);
                i.putExtra("nombre", nombre);
                i.putExtra("usuario", usuario);
                i.putExtra("email", email);
                startActivity(i);
            }
        });
    }

    private void obtenerDatos(String usuario) {
        Data datos = new Data.Builder()
                .putString("usuario",usuario)
                .build();

        OneTimeWorkRequest obtenerDatosOtwr= new OneTimeWorkRequest.Builder(obtenerDatosUsuarioWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(obtenerDatosOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta, teniendo en cuenta que es de la forma CODIGO#VALOR
                        if(workInfo != null && workInfo.getState().isFinished()){
                            Data outputData = workInfo.getOutputData();
                            String resultado = outputData.getString("resultado");
                            if (resultado!=null) {
                                JSONParser parser = new JSONParser();
                                JSONObject json = null;
                                try {
                                    json = (JSONObject) parser.parse(resultado);
                                    ProfileActivity.this.nombre=(String) json.get("nombrecompleto");
                                    ProfileActivity.this.email=(String) json.get("correo");
                                    //He obtenido datos, muestro los campos
                                    visibilidadCampos(View.VISIBLE);
                                    //Cargo foto de perfil
                                    String fotoUri=(String) json.get("foto");

                                    ImageView imgPerfil = findViewById(R.id.imgPerfil);
                                    if (fotoUri!=null && !fotoUri.equals("")) { //Si hay foto de perfil

                                        ProfileActivity.this.foto = fotoUri; //Guardo la uri de la foto

                                        //Descargo la foto del servidor
                                        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/"+fotoUri;
                                        URL destino = null;
                                        try {
                                            destino = new URL(direccion);
                                            HttpURLConnection conn = (HttpURLConnection) destino.openConnection();
                                            int responseCode = 0;
                                            responseCode = conn.getResponseCode();
                                            if (responseCode == HttpsURLConnection.HTTP_OK) {
                                                Bitmap elBitmap = BitmapFactory.decodeStream(conn.getInputStream()); //Obtengo la imagen
                                                imgPerfil.setImageBitmap(elBitmap);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                    }
                                    else{ //Foto por defecto
                                        imgPerfil.setImageDrawable(getDrawable(R.drawable.imgavatar));
                                    }
                                    //Cargo nombre completo
                                    TextView textNombre = findViewById(R.id.textNombre);
                                    textNombre.setText(nombre);

                                    //Cargo datos juego
                                    int preguntasCorrectas=0;
                                    int preguntasIncorrectas=0;
                                    if (json.get("preguntasCorrectas")!=null){
                                        preguntasCorrectas=Integer.parseInt((String) json.get("preguntasCorrectas"));
                                    }
                                    if (json.get("preguntasIncorrectas")!=null){
                                        preguntasIncorrectas=Integer.parseInt((String) json.get("preguntasIncorrectas"));
                                    }

                                    //Obtengo el porcentaje de preguntas correctas
                                    if (preguntasCorrectas==0 && preguntasIncorrectas==0){ //Nunca ha jugado
                                        TextView textpreguntasCorrectas= findViewById(R.id.preguntasCorrectasText);
                                        textpreguntasCorrectas.setText(getString(R.string.menuPerfil_noJuego));
                                    }
                                    else { //He jugado
                                        ProgressBar progressBar=findViewById(R.id.preguntasProgressBar);
                                        int porcentajeCorrecto=0;
                                        int porcentajeIncorrecto=0;
                                        TextView textpreguntasCorrectas= findViewById(R.id.preguntasCorrectasText);
                                        if (preguntasCorrectas==0){ //No ha acertado ninguna pregunta
                                            textpreguntasCorrectas.setText(getString(R.string.menuPerfil_noAcPregunta));
                                        }
                                        else if (preguntasCorrectas==1){ //Si solo ha acertado una pregunta
                                            textpreguntasCorrectas.setText(getString(R.string.menuPerfil_preguntas1)+" "+preguntasCorrectas+" "+getString(R.string.menuPerfil_1pregunta));
                                        }
                                        else { //Si ha acertado mas de una
                                            textpreguntasCorrectas.setText(getString(R.string.menuPerfil_preguntas1)+" "+preguntasCorrectas+" "+getString(R.string.menuPerfil_preguntas2));
                                        }

                                        if (preguntasIncorrectas==0 && preguntasCorrectas>0) {  //Nunca ha fallado ninguna pregunta
                                            porcentajeCorrecto=100;
                                        }
                                        else { //Calculo el porcentaje de correctas
                                            porcentajeCorrecto= (preguntasCorrectas*100/ (preguntasIncorrectas+preguntasCorrectas));
                                            porcentajeIncorrecto=100;
                                        }
                                        progressBar.setProgress(porcentajeCorrecto);
                                        progressBar.setSecondaryProgress(porcentajeIncorrecto);

                                        TextView txtPreguntasCorrectas=findViewById(R.id.textoPreguntasCorrectas);
                                        TextView txtPreguntasIncorrectas=findViewById(R.id.textoPreguntasIncorrectas);
                                        txtPreguntasCorrectas.setText(""+preguntasCorrectas);
                                        txtPreguntasCorrectas.setTextColor(Color.parseColor("#33691e"));
                                        txtPreguntasIncorrectas.setText(""+preguntasIncorrectas);
                                        txtPreguntasIncorrectas.setTextColor(Color.parseColor("#ff4400"));
                                    }
                                    //Cargo datos duelo
                                    int duelosGanados=0;
                                    int duelosTotales=0;
                                    if (json.get("duelosGanados")!=null){
                                        duelosGanados=Integer.parseInt((String) json.get("duelosGanados"));
                                    }
                                    if (json.get("duelosJugados")!=null){
                                        duelosTotales=Integer.parseInt((String) json.get("duelosJugados"));
                                    }
                                    int duelosPerdidos=duelosTotales-duelosGanados;

                                    //Obtengo el porcentaje de duelos
                                    if (duelosGanados==0 && duelosPerdidos==0){ //Nunca ha jugado
                                        TextView textDuelosGanados= findViewById(R.id.duelosGanadosText);
                                        textDuelosGanados.setText(getString(R.string.menuPerfil_noDuelo));
                                    }
                                    else { //He jugado
                                        ProgressBar progressBar=findViewById(R.id.duelosProgressBar);
                                        int porcentajeGanado=0;
                                        int porcentajePerdido=0;
                                        TextView textDuelosGanados= findViewById(R.id.duelosGanadosText);
                                        if (duelosGanados==0){ //No ha ganado ningun duelo
                                            textDuelosGanados.setText(getString(R.string.menuPerfil_noGanaDuelo));
                                        }
                                        else if (duelosGanados==1){ //Si solo ha ganado un duelo
                                            textDuelosGanados.setText(getString(R.string.menuPerfil_duelo1)+" "+duelosGanados+" "+getString(R.string.menuPerfil_1duelo));
                                        }
                                        else { //Si ha ganado mas de un duelo
                                            textDuelosGanados.setText(getString(R.string.menuPerfil_duelo1)+" "+duelosGanados+" "+getString(R.string.menuPerfil_duelo2));
                                        }

                                        if (duelosPerdidos==0 && duelosGanados>0) {  //Ha ganado todos los duelos
                                            porcentajeGanado=100;
                                        }
                                        else { //Calculo el porcentaje de duelos ganados
                                            porcentajeGanado= duelosGanados*100/duelosTotales;
                                            porcentajePerdido=100;
                                        }
                                        progressBar.setProgress(porcentajeGanado);
                                        progressBar.setSecondaryProgress(porcentajePerdido);

                                        TextView txtDuelosGanados=findViewById(R.id.textoDuelosGanados);
                                        TextView txtDuelosPerdidos=findViewById(R.id.textoDuelosPerdidos);
                                        txtDuelosGanados.setText(""+duelosGanados);
                                        txtDuelosGanados.setTextColor(Color.parseColor("#33691e"));
                                        txtDuelosPerdidos.setText(""+duelosPerdidos);
                                        txtDuelosPerdidos.setTextColor(Color.parseColor("#ff4400"));
                                    }


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                }
                            }

                        }
                    });
        WorkManager.getInstance(getApplicationContext()).enqueue(obtenerDatosOtwr);
    }

    private void cerrarSesion(){
        //Eliminamos la sesion almacenada en SharedPreferences
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(generateAuthToken.AUTH_CONTEXT, Context.MODE_PRIVATE); //Obtengo las preferencias de autenticacion
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().apply();
        //Finalizamos actividad y volvemos al menu de login
        Toast.makeText(getApplicationContext(), getString(R.string.menuPerfil_cerradaSesion), Toast.LENGTH_LONG).show();
        finish(); //Finalizo mi actividad
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }
    private void visibilidadCampos(int visibility){
        findViewById(R.id.profileLayout).setVisibility(visibility);
    }

    public void onBackPressed(){
        //Pulsa el boton atras, reimplementacion para volver al menu principal
        finish(); //Finalizo mi actividad
        Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
        i.putExtra("usuario",usuario);
        startActivity(i);
    }
}
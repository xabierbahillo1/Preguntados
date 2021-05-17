package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.das.preguntados.Common.ActivityVertical;
import com.das.preguntados.Common.generateAuthToken;
import com.das.preguntados.R;
import com.das.preguntados.WS.comprobarTokenAuthWS;

public class SplashActivity extends ActivityVertical {
    /*SPLASH SCREEN DEL INICIO DE LA APLICACION
        Mira si hay alguna sesion guardada
            - Si hay, comprueba si es correcta y envia a MainMenuActivity
            - Si no hay o sesion incorrecta envia a LoginActivity
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestionarInicioSesionAutomatico();
    }


    private void gestionarInicioSesionAutomatico(){
        //Compruebo si tengo un usuario y token guardados en SharedPreferences
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(generateAuthToken.AUTH_CONTEXT, Context.MODE_PRIVATE); //Obtengo las preferencias de autenticacion
        String usuario= sharedPref.getString("usuario",null); //Obtengo el usuario de las preferencias, si no existe devuelvo null
        String token= sharedPref.getString("token",null); //Obtengo el token de las preferencias, si no existe devuelvo null
        if (usuario!=null && token!=null){ //Si ambos no son nulos
            //Lanzo una peticion contra la base de datos para comprobar si son datos correctos
            Log.d("inicioSesionAutomatico","Se ha encontrado sesion para el usuario: "+usuario);
            Data datos = new Data.Builder()
                    .putString("usuario",usuario)
                    .putString("token",token)
                    .build();

            OneTimeWorkRequest comprobarTokenOtwr= new OneTimeWorkRequest.Builder(comprobarTokenAuthWS.class).setInputData(datos)
                    .build();

            WorkManager.getInstance(this).getWorkInfoByIdLiveData(comprobarTokenOtwr.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            //Trato la respuesta, teniendo en cuenta que es de la forma CODIGO#VALOR
                            if(workInfo != null && workInfo.getState().isFinished()){

                                String resultado=workInfo.getOutputData().getString("resultado");
                                if (resultado!=null) {
                                    String[] respuesta= resultado.split("#");
                                    if (respuesta[0].equals("ERR")){
                                        //Hay un error, por tanto llevo a pantalla login y muestro un mensaje de sesion expirada
                                        Toast.makeText(getApplicationContext(), getString(R.string.login_sessionExpired), Toast.LENGTH_LONG).show();
                                        //Imprimo el error por logs
                                        Log.d("inicioSesionAutomatico","Error al comprobar token: "+respuesta[1]);
                                        //Limpio las preferencias, eliminando el usuario y token guardados
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.clear().apply();
                                        esperar(); //Espero dos segundos para que se vea el SplashScreen
                                        finish();
                                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(i);
                                    }
                                    else if (respuesta[0].equals("OK")){
                                        //Datos de sesion correctos, por tanto lanzo la actividad principal
                                        Log.d("inicioSesionAutomatico","Los datos de sesión son correctos");
                                        finish(); //Finalizo actividad actual
                                        Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
                                        i.putExtra("usuario",usuario); //Envio los datos del usuario
                                        startActivity(i);
                                    }
                                }

                            }
                        }
                    });
            WorkManager.getInstance(getApplicationContext()).enqueue(comprobarTokenOtwr);
        }
        else{
            Log.d("inicioSesionAutomatico","No se ha encontrado ninguna sesión guardada");
            //Llevo al login
            esperar(); //Espero dos segundos para que se vea el splashScreen
            finish();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }

    }

    private void esperar(){
        //Funcion para dejar la actividad durante 1 segundo
        try {
            //Ponemos a "Dormir" el programa durante los ms que queremos
            Thread.sleep(1*1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
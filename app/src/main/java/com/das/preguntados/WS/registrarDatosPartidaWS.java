package com.das.preguntados.WS;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class registrarDatosPartidaWS extends Worker {
    /*WEBSERVICE para registrar una partida en la base de datos
        Respuesta: CODIGO#VALOR
            CODIGO: ERR -> Error, OK -> Partida guardada
            VALOR: En el caso de error, literal con el error para imprimir por los logs
     */
    public registrarDatosPartidaWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/registrarDatosPartida.php";

        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            //Paso los datos del usuario y token como parametro
            String usuario= getInputData().getString("usuario");
            int modo=getInputData().getInt("modo",0);
            int puntuacion=getInputData().getInt("puntuacion",0);
            int preguntasCorrectas=getInputData().getInt("preguntasCorrectas",0);
            int preguntasIncorrectas=getInputData().getInt("preguntasIncorrectas",0);
            if (modo!=0){ //Si el modo es correcto, se pasan los datos como parametro
                String parametros = "usuario="+usuario+"&modo="+modo+"&puntuacion="+puntuacion+"&preguntasCorrectas="+preguntasCorrectas+"&preguntasIncorrectas="+preguntasIncorrectas;
                out.print(parametros);
            }
            out.close();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) { //Si 200 OK
                //Obtengo la respuesta recibida
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, result = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                inputStream.close();
                //Devuelvo la respuesta para tratarla desde LoginActivity
                String[] resultado=result.split("#");
                Log.d("registrarDatosPartida"+resultado[0],resultado[1]);
                return Result.success();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

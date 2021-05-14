package com.das.preguntados.WS;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class registrarDatosDueloWS extends Worker {
    /*WEBSERVICE para registrar una partida en la base de datos
        Respuesta: CODIGO#VALOR
            CODIGO: ERR -> Error, OK -> Partida guardada
            VALOR: En el caso de error, literal con el error para imprimir por los logs
     */
    public registrarDatosDueloWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/registrarDuelo.php";

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
            //Paso los datos del duelo como parametro
            String host= getInputData().getString("host");
            String guest= getInputData().getString("guest");
            int aciertosHost=getInputData().getInt("aciertosHost",0);
            int aciertosGuest=getInputData().getInt("aciertosGuest",0);
            String ganador = getInputData().getString("ganador");
            String parametros = "host="+host+"&guest="+guest+"&aciertosHost="+aciertosHost+"&aciertosGuest="+aciertosGuest+"&ganador="+ganador;
            out.print(parametros);
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
                //Imprimo la respuesta por los logs
                String[] resultado=result.split("#");
                Log.d("registrarDatosDuelo"+resultado[0],resultado[1]);
                return Result.success();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

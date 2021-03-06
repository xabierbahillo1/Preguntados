package com.das.preguntados.WS;
import android.content.Context;

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

public class iniciarSesionWS extends Worker {
    /*WEBSERVICE para iniciar sesion en la aplicacion
        Es necesario pasar como parametros: Usuario, Clave y Token de autenticacion
        Respuesta: CODIGO#VALOR
            CODIGO: ERR -> Error, OK -> Login correcto
            VALOR: En el caso de error, referencia para obtener el string en strings.xml
     */
    public iniciarSesionWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-242-79-204.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/iniciarSesion.php";
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
            String clave = getInputData().getString("clave");
            String token=getInputData().getString("token");
            String parametros = "usuario="+usuario+"&clave="+clave+"&token="+token;
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
                //Devuelvo la respuesta para tratarla desde LoginActivity
                Data resultados = new Data.Builder()
                        .putString("resultado",result)
                        .build();
                return Result.success(resultados);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return Result.failure();
        }
        return null;
    }

}

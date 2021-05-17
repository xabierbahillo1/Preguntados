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

public class ActualizarPerfilWS extends Worker {
    /*WEBSERVICE para registrar un usuario en la aplicación.
        Es necesario pasar como parametros: Email, Nombre Completo, Usuario, Clave y Token de sesion
        Respuesta: CODIGO#VALOR
            CODIGO: ERR -> Error, OK -> Registro realizado
            VALOR: En el caso de error, referencia para obtener el string en strings.xml
     */
    public ActualizarPerfilWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/registrarUsuario.php";

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
            //Paso los datos del usuario a registrar como parametro
            String correo= getInputData().getString("email");
            String nombrecompleto= getInputData().getString("nombre");
            String usuario= getInputData().getString("usuario");
            String parametros = "correo="+correo+"&nombrecompleto="+nombrecompleto+"&usuario="+usuario;
            out.print(parametros);
            out.close();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) { //Si 200 OK
                //Obtengo la respuesta recibida

                return Result.success();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
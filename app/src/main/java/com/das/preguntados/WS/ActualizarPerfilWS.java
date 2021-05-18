package com.das.preguntados.WS;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActualizarPerfilWS extends Worker {
    /*WEBSERVICE para modificar los datos de un usuario en la aplicacion.
        Es necesario pasar como parametros: Email, Nombre Completo, Usuario y Foto
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
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/actualizarUsuario.php";

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
            String uriFoto= getInputData().getString("foto"); //uri de la foto
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("correo",correo)
                    .appendQueryParameter("nombrecompleto",nombrecompleto)
                    .appendQueryParameter("usuario", usuario);
            if (uriFoto!=null){
                Uri miUri= Uri.parse(uriFoto);
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),miUri);
                bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/2,bitmap.getHeight()/2,true); //Reescalado para evitar consumir muchos recursos
                ByteArrayOutputStream baos = new ByteArrayOutputStream(6000);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10 , baos);
                byte[] blob = baos.toByteArray();
                String fotoen64= Base64.encodeToString(blob, Base64.DEFAULT);
                builder.appendQueryParameter("foto", fotoen64);
            }
            String parametrosURL = builder.build().getEncodedQuery();
            out.print(parametrosURL);
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
                //Devuelvo la respuesta para tratarla desde EditProfileActivity
                Data resultados = new Data.Builder()
                        .putString("resultado",result)
                        .build();
                return Result.success(resultados);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

package com.das.preguntados.WS;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.das.preguntados.GameManager.ColeccionPreguntas;
import com.das.preguntados.GameManager.Pregunta;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class obtenerPreguntasWS extends Worker {
    /*WEBSERVICE para obtener las preguntas de la aplicacion
        Parametros: Idioma (0 por defecto, 1 ingles)
                    Genero de la pregunta (opcional)
        Respuesta: CODIGO#VALOR o JSON con las preguntas
            CODIGO: ERR -> Error,
            VALOR: En el caso de error, cadena de caracteres para imprimir por los logs
     */
    public obtenerPreguntasWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/obtenerPreguntas.php";

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
            String idioma= getInputData().getString("idioma");
            String genero= getInputData().getString("genero");
            String usuario= getInputData().getString("usuario");
            String clave= getInputData().getString("clave");
            String token=getInputData().getString("token");
            String parametros = "idioma="+idioma;
            if (genero!=null){
                parametros+="&genero="+genero;
            }
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
                //Trato la respuesta
                if (result.contains("ERR#")){ //Tiene un error
                    Log.d("obtenerPreguntas",result.split("#")[1]);
                    return Result.failure();
                }
                //Obtengo el JSON con las preguntas
                JSONParser parser = new JSONParser();
                JSONArray json = (JSONArray) parser.parse(result);
                //Obtengo la lista de preguntas y la reseteo
                ColeccionPreguntas misPreguntas= ColeccionPreguntas.obtenerMiColeccion();
                misPreguntas.resetear();
                //Cargo los datos del JSON en la lista de preguntas
                for (int i=0;i<json.size();i++) { //Recorro el json
                    JSONObject dataJson= (JSONObject) json.get(i);
                    String generoP=(String) dataJson.get("genero");
                    String texto=(String) dataJson.get("texto");;
                    String textoOpcionA=(String) dataJson.get("textoOpcionA");
                    String textoOpcionB=(String) dataJson.get("textoOpcionB");
                    String textoOpcionC=(String) dataJson.get("textoOpcionC");
                    String opcionGanadora=(String) dataJson.get("opcionGanadora");
                    misPreguntas.anadirPregunta(new Pregunta(generoP,texto,textoOpcionA,textoOpcionB,textoOpcionC,opcionGanadora));
                }
                return Result.success();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

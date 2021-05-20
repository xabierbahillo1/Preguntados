package com.das.preguntados.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.das.preguntados.Adapters.RankingListAdapter;
import com.das.preguntados.R;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class RankingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        TextView textRanking = findViewById(R.id.textRanking1);
        textRanking.setText(R.string.textRanking1);

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setText(R.string.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnChangeRanking = findViewById(R.id.btnChangeRanking);
        btnChangeRanking.setText(R.string.btnRankingChange);
        btnChangeRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RankingActivityAlt.class);
                startActivity(i);
                finish();
            }
        });

        ListView list = findViewById(R.id.rankingList);
        ArrayList[] listas = obtenerInfo();

        ArrayList<String> nameList = listas[0];
        ArrayList<String> puntuacionList = listas[1];
        ArrayList<Bitmap> fotoList = listas[2];
        for (Bitmap bitmap : fotoList) {
            System.out.println(bitmap);
        }

        RankingListAdapter arrayAdapter = new RankingListAdapter(this, nameList, puntuacionList, fotoList);
        list.setAdapter(arrayAdapter);
    }

    public ArrayList<String>[] obtenerInfo() {
        ArrayList<String> nameList = new ArrayList<String>();
        ArrayList<String> puntuacionList = new ArrayList<String>();
        ArrayList<Bitmap> fotoList = new ArrayList<Bitmap>();
        // Forzar la ejecución en primer plano
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Petición http
        String direccion = "http://ec2-54-242-79-204.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/obtenerRanking1.php";
        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.close();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                // Parsea y devuelve el resultado
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, result = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                inputStream.close();

                // Parsear la respuesta del servidor php
                JSONParser parser = new JSONParser();
                try {
                    // Obtener el array de anuncios
                    JSONArray jsonArray = (JSONArray) parser.parse(result);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        // Por cada entrada, obtiene los datos y crea un anuncio, añadiéndolo a la lista local
                        JSONObject json = (JSONObject) jsonArray.get(i);
                        String nombreCompleto = (String) json.get("nombre");
                        String puntuacion = (String) json.get("puntuacion");
                        String foto = (String) json.get("foto");
                        nameList.add(nombreCompleto);
                        puntuacionList.add(puntuacion);

                        if (foto == null || foto.equals("")) {
                            Bitmap elBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.imgavatar);
                            fotoList.add(elBitmap);
                        } else {
                            String direccion2 = "http://ec2-54-242-79-204.compute-1.amazonaws.com/xbahillo001/WEB/preguntados/" + foto;
                            try {
                                URL destino2 = new URL(direccion2);
                                HttpURLConnection urlConnection2 = (HttpURLConnection) destino2.openConnection();
                                int responseCode2 = 0;
                                responseCode2 = urlConnection2.getResponseCode();
                                if (responseCode2 == HttpURLConnection.HTTP_OK) {
                                    System.out.println("////////////////////");
                                    System.out.println(foto);
                                    System.out.println("////////////////////");
                                    Bitmap elBitmap = BitmapFactory.decodeStream(urlConnection2.getInputStream());
                                    System.out.println(elBitmap);
                                    fotoList.add(elBitmap);
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    ArrayList[] listas = new ArrayList[3];

                    listas[0] = nameList;
                    listas[1] = puntuacionList;
                    listas[2] = fotoList;

                    return listas;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
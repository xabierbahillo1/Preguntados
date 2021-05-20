package com.das.preguntados.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.das.preguntados.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RankingListAdapter extends BaseAdapter {

    private Context contexto;
    private LayoutInflater inflater;
    private ArrayList<String> nombres;
    private ArrayList<String> puntuaciones;
    private ArrayList<Bitmap> fotos;

    // Constructora de la clase
    public RankingListAdapter(Context pcontext, ArrayList<String> pNombres, ArrayList<String> pPuntuaciones, ArrayList<Bitmap> pFotos) {
        contexto = pcontext;
        nombres = pNombres;
        puntuaciones = pPuntuaciones;
        fotos = pFotos;
        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return nombres.size();
    }

    @Override
    public Object getItem(int position) {
        return nombres.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view=inflater.inflate(R.layout.ranking_list_row, null);

        // Configurar los elementos del layout de la fila
        TextView nombre= (TextView) view.findViewById(R.id.textFilaListaTitulo);
        nombre.setText(nombres.get(position));

        TextView puntuacion= (TextView) view.findViewById(R.id.textFilaListaPuntuacion);
        puntuacion.setText(puntuaciones.get(position));

        CircleImageView img=(CircleImageView) view.findViewById(R.id.listImage);
        //Decodificar la imagen de base 64 a BitMap y ponerla en el imageView
        Bitmap foto = fotos.get(position);
        img.setImageBitmap(foto);

        return view;
    }
}

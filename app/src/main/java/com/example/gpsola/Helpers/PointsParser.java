package com.example.gpsola.Helpers;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.example.gpsola.Models.Destino;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PointsParser extends AsyncTask <String, Integer, Destino> {
    private TaskLoadedCallback taskCallback;
    private String directionMode;

    public PointsParser(Context taskCallback, String directionMode) {
        this.taskCallback = (TaskLoadedCallback) taskCallback;
        this.directionMode = directionMode;
    }

    @Override
    protected Destino doInBackground(String... jsonData) {
        JSONObject jsonObject;
        Destino destino = new Destino();

        try{
            jsonObject = new JSONObject(jsonData[0]);
            Log.d("JsonObject", jsonData[0]);
            DataParser parser = new DataParser();
            //Iniciando o parsing Data
            destino = parser.parse(jsonObject);
        }catch (Exception e){
            Log.e("Erro de Parser", e.toString());
        }


        return destino;
    }

    @Override
    protected void onPostExecute(Destino destino) {
        ArrayList<LatLng> points;

        //Percorrendo as rotas no objeto do destino
        for(int i= 0; i < destino.getRoutes().size(); i++){
            points =  new ArrayList<>();
            destino.setLineOptions(new PolylineOptions());

            List<HashMap<String,String>> path = destino.getRoutes().get(i);

            for(int j =0;j < path.size(); j++){
                HashMap<String , String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat,lng);
                points.add(position);
            }
            destino.getLineOptions().addAll(points);
            if(directionMode.equalsIgnoreCase("walking")){
                destino.getLineOptions().width(10);
                destino.getLineOptions().color(Color.MAGENTA);
            }else{
                destino.getLineOptions().width(20);
                destino.getLineOptions().color(Color.BLACK);
            }
            Log.d("onPostExecute", "onPostExecute lineoptions decoded");
        }

        //Desenhando as Rotas no Google Maps
        if(destino.getLineOptions() != null){
            taskCallback.onTaskdone(destino);
        }else{
            Log.d("mylog", "Sem polilinhas desenhadas");
        }
    }
}

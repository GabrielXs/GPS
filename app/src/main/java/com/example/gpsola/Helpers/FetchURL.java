package com.example.gpsola.Helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchURL extends AsyncTask<String, Void,String> {
    Context mContext;
    String directionMode = "driving";

    public FetchURL(Context context) {
        this.mContext = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String data = "";
        directionMode = strings[1];

        try {
            data = downladoUrl(strings[0]);
        }catch (Exception e){
            Log.e("Downlad URL", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        PointsParser parserTask = new PointsParser(mContext,directionMode);
        parserTask.execute(s);
    }

    private String downladoUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(strUrl);
            //Criando Conexao com o Google
            urlConnection = (HttpURLConnection) url.openConnection();
            //Conectando  para url
            urlConnection.connect();
            //Lendo os dados da Url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";

            while((line = br.readLine())!= null){
                sb.append(line);
            }

            data = sb.toString();
            Log.d("Download URL", "Downloaded URL" + data);
        }catch (Exception e){
            Log.e("download url", String.format("Exception downloading URL %s", e.toString()));
        }finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}

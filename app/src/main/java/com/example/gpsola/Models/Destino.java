package com.example.gpsola.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;

public class Destino {
    private Marker currentDestinoMarker;
    private LatLng currentDestinoLatLng;
    private String duracao;
    private String Endereco;
    private List<List<HashMap<String,String>>> routes;
    PolylineOptions lineOptions;

    public PolylineOptions getLineOptions() {
        return lineOptions;
    }

    public void setLineOptions(PolylineOptions lineOptions) {
        this.lineOptions = lineOptions;
    }

    public List<List<HashMap<String, String>>> getRoutes() {
        return routes;
    }

    public void setRoutes(List<List<HashMap<String, String>>> routes) {
        this.routes = routes;
    }

    public Marker getCurrentDestinoMarker() {
        return currentDestinoMarker;
    }

    public void setCurrentDestinoMarker(Marker currentDestinoMarker) {
        this.currentDestinoMarker = currentDestinoMarker;
    }

    public LatLng getCurrentDestinoLatLng() {
        return currentDestinoLatLng;
    }

    public void setCurrentDestinoLatLng(LatLng currentDestinonLatLng) {
        this.currentDestinoLatLng = currentDestinonLatLng;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public String getEndereco() {
        return Endereco;
    }

    public void setEndereco(String endereco) {
        Endereco = endereco;
    }
}

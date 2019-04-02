package com.example.gpsola.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Tecnico {

    private Marker currentLocationMarker;
    private LatLng currentLocationLatLng;

    public Tecnico(){

    }

    public Tecnico(Marker currentLocationMarker, LatLng currentLocationLatLng) {
        this.currentLocationMarker = currentLocationMarker;
        this.currentLocationLatLng = currentLocationLatLng;
    }

    public Marker getCurrentLocationMarker() {
        return currentLocationMarker;
    }

    public void setCurrentLocationMarker(Marker currentLocationMarker) {
        this.currentLocationMarker = currentLocationMarker;
    }

    public LatLng getCurrentLocationLatLng() {
        return currentLocationLatLng;
    }

    public void setCurrentLocationLatLng(LatLng currentLocationLatLng) {
        this.currentLocationLatLng = currentLocationLatLng;
    }
}

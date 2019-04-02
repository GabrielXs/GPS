package com.example.gpsola;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.gpsola.Helpers.FetchURL;
import com.example.gpsola.Helpers.TaskLoadedCallback;
import com.example.gpsola.Models.Destino;
import com.example.gpsola.Models.Tecnico;
import com.example.gpsola.Utils.Const;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private GoogleMap mMap;
    private Destino destino = new Destino();
    private Tecnico tecnico;
    private TextView txtdestino,txtDuracao;

    //Pegando a ultima Localização conhecida
    private FusedLocationProviderClient fusedLocationProviderClient;
    private  Boolean requestLocationUpdates = true;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Criando a rota
    private Polyline currentPolyline;

    //Gravando no Banco de dados
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FirebaseApp.initializeApp(this);
        txtdestino = findViewById(R.id.txtDestino);
        txtDuracao = findViewById(R.id.txtDuracao);

        createLocationRequest();

        //Trocar o controlador requestLocationUpdates
        updateValuesFromBundle(savedInstanceState);

        tecnico = new Tecnico();

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("GPS").child("Tecnico");
                    //Atualizar a interface do usuário com dados de localização
                    if(tecnico.getCurrentLocationMarker() != null){
                        tecnico.getCurrentLocationMarker().remove();
                    }

                    //Adicionando o marcador ao tecnico
                    tecnico.setCurrentLocationLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(tecnico.getCurrentLocationLatLng());
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    markerOptions.title("Localização Atual do Tecnico");

                    tecnico.setCurrentLocationMarker(mMap.addMarker(markerOptions));

                    new FetchURL(MainActivity.this)
                            .execute(
                                    getUrl(tecnico.getCurrentLocationLatLng(),
                                            destino.getCurrentDestinoLatLng(),
                                            "driving"
                                    ),
                            "driving");

                    //Movendo para a nova Localizaçao do Tecnico
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                                            .zoom(12.5f)
                                                            .target(tecnico.getCurrentLocationLatLng())
                                                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    //Gravando no Banco de dados do Firebase
                    mDatabase.setValue(tecnico.getCurrentLocationLatLng());

                }
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Const.REQUESTING_LOCATION_UPDATES_KEY,requestLocationUpdates);
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if(savedInstanceState == null){
            return;
        }

        //Atualizando o valor para requestingLocationUpdates para o Bundle
        if(savedInstanceState.keySet().contains(Const.REQUESTING_LOCATION_UPDATES_KEY)){
            requestLocationUpdates = savedInstanceState.getBoolean(Const.REQUESTING_LOCATION_UPDATES_KEY);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(requestLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdate();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Ao iniciar o Mapa ja marcar o o destino
        mMap = googleMap;

        //Definindo o latitude e longitude
        destino.setCurrentDestinoLatLng(new LatLng(-22.884990,-43.499221));

        //Adicionando e atribuindo caracteristica ao marcador
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(destino.getCurrentDestinoLatLng());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.title("Destino");
        destino.setCurrentDestinoMarker(mMap.addMarker(markerOptions));


    }


    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(this, new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    //As Configurações de localização não estão satisfeita, mas isso pode ser corrigido
                    //mostrando ao usuário uma caixa de dialogo

                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,Const.REQUEST_CHECK_SETTINGS);
                    }catch (IntentSender.SendIntentException sendEx){
                        Log.e("ConfiguracaoSettings", sendEx.toString());
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {

        ArrayList<String> permissao = new ArrayList<>();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissao.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissao.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(!permissao.isEmpty()){
            String [] request = new String[permissao.size()];
            for(int i = 0; i < permissao.size(); i++){
                request[i] = permissao.get(i);
            }
           ActivityCompat.requestPermissions(MainActivity.this, request, Const.REQUEST_GPS);
        }else{
          // Pegar Localização Atual do Tecnico
          fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case Const.REQUEST_GPS:
                    if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                        this.recreate();
                    }
                break;
        }
    }

    private String getUrl(LatLng dest, LatLng tec, String directionMode){
        //Destino da rota
        String str_destino = "origin=" + dest.latitude + "," + dest.longitude;

        //Rota do Tecnico
        String str_tecnico = "destination=" + tec.latitude + "," + tec.longitude;

        //Modo de Viagem
        String mode = "mode="+directionMode;
        //Parametros
        String parameters = str_destino + "&" + str_tecnico + "&" + mode;
        //Saida
        String output = "json";
        //Construindo a url do WebService
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_direction_key);

        return  url;
    }
    @Override
    public void onTaskdone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();


        destino.setLineOptions(((Destino) values[0]).getLineOptions());
        destino.setEndereco(((Destino) values[0]).getEndereco());
        destino.setDuracao(((Destino) values[0]).getDuracao());
        currentPolyline = mMap.addPolyline(destino.getLineOptions());

        txtDuracao.setText(destino.getDuracao());
        txtdestino.setText(destino.getEndereco());
    }
}

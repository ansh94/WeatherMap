package com.anshdeep.weathermap;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.anshdeep.weathermap.api.models.Coord;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PolyActivity extends AppCompatActivity implements OnMapReadyCallback {

    private List<Coord> coordList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poly);

        readFromSharedPreferences();

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.poly_map);
        mapFragment.getMapAsync(this);
    }

    private void readFromSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPrefs.getString("MyLocationList", null);
        Type type = new TypeToken<ArrayList<Coord>>() {
        }.getType();
        coordList = gson.fromJson(json, type);


        if (coordList != null) {
            for (Coord coord : coordList) {
                Log.d("PolyActivity", "lat: " + coord.getLat());
                Log.d("PolyActivity", "lon: " + coord.getLon());
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add polylines to the map.
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);

        if (coordList != null) {
            int size = coordList.size();

            // gets the last 5 location coordinates searched by making a new sublist
            if (size > 5) {
                List<Coord> newCoordList = coordList.subList(size - 5, size);
                for (Coord c : newCoordList) {
                    double lat = c.getLat();
                    double lon = c.getLon();
                    options.add(new LatLng(lat, lon));
                }
            } else {
                for (Coord coord : coordList) {
                    double lat = coord.getLat();
                    double lon = coord.getLon();
                    options.add(new LatLng(lat, lon));
                }
            }


            Polyline mypolyline = googleMap.addPolyline(options);

            // Position the map's camera near first searched location,
            // and set the zoom factor so most of the location shows on the screen.
            Coord firstCoord = coordList.get(0);
            double lat = firstCoord.getLat();
            double lon = firstCoord.getLon();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 4));
        }


    }
}

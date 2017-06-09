package com.anshdeep.weathermap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.anshdeep.weathermap.api.models.Coord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_buttonx) Button buttonX;
    @BindView(R.id.btn_submit) Button buttonSubmit;
    @BindView(R.id.editText) EditText cityName;

    List<Coord> coordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        readFromSharedPreferences();
    }

    @OnClick(R.id.btn_submit)
    public void submit() {
        if (cityName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a city name!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, LocationWeatherDetailActivity.class);
            intent.putExtra("CityName", cityName.getText().toString());
            startActivity(intent);
        }

    }

    private void readFromSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = sharedPrefs.getString("MyLocationList", null);
        Type type = new TypeToken<ArrayList<Coord>>() {
        }.getType();
        coordList = gson.fromJson(json, type);
        if (coordList == null) {
            coordList = new ArrayList<>();
        }
    }

    @OnClick(R.id.btn_buttonx)
    public void showPolyline() {
        readFromSharedPreferences();
        if (coordList == null || coordList.size() < 2) {
            Toast.makeText(this, "Search for at least two locations first to show polylines", Toast.LENGTH_SHORT).show();
        } else {
            Intent polyIntent = new Intent(this, PolyActivity.class);
            startActivity(polyIntent);
        }

    }
}

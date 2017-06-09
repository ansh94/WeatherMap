package com.anshdeep.weathermap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anshdeep.weathermap.api.models.Coord;
import com.anshdeep.weathermap.api.models.Main;
import com.anshdeep.weathermap.api.models.Weather;
import com.anshdeep.weathermap.api.models.WeatherResponse;
import com.anshdeep.weathermap.api.service.WeatherServiceInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationWeatherDetailActivity extends AppCompatActivity {

    private String cityName = "Delhi";

    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static final String apiKey = "0f9289eda2f5d6283e0adfb83b382f3c";

    List<Weather> weatherList = new ArrayList<>();
    List<Coord> coordList;

    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.cityNameValue) TextView cityNameText;
    @BindView(R.id.mainWeatherValue) TextView mainWeatherText;
    @BindView(R.id.tempValue) TextView tempText;
    @BindView(R.id.pressureValue) TextView pressureText;
    @BindView(R.id.humidityValue) TextView humidityText;
    @BindView(R.id.visibilityValue) TextView visibilityText;
    @BindView(R.id.weatherDescValue) TextView weatherDesc;
    @BindView(R.id.locationIcon) ImageButton mapIcon;

    String latitude;
    String longitude;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_weather_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra("CityName")) {
            cityName = getIntent().getStringExtra("CityName");
        }

        readFromSharedPreferences();
        performLoading();

    }

    public void performLoading() {
        //check network connection
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            getRetrofitResponse(cityName, apiKey);
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void getRetrofitResponse(String cityName, String apiKey) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherServiceInterface serviceRequest = retrofit.create(WeatherServiceInterface.class);

        Call<WeatherResponse> call = serviceRequest.getWeather(cityName, apiKey);

        /*
          enqueue() asynchronously sends the request and notifies your app with a callback when a response comes back.
          Since this request is asynchronous, Retrofit handles it on a background thread so that the main UI thread
          isn't blocked or interfered with.
         */
        call.enqueue(new Callback<WeatherResponse>() {

            /*
            onResponse(): invoked for a received HTTP response. This method is called for a response that can be correctly
            handled even if the server returns an error message. So if you get a status code of 404 or 500, this method will
            still be called. To get the status code in order for you to handle situations based on them, you can use the
            method response.code().You can also use the isSuccessful() method to find out if the status code is in
            the range 200-300, indicating success.
             */

            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                int statusCode = response.code();

                if (statusCode != 200) {
                    Log.d("LocationWeatherDetail", "Some error in getting data. Error code: " + statusCode);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LocationWeatherDetailActivity.this, "Some error in getting data. Error code: " + statusCode, Toast.LENGTH_LONG).show();
                    finish();
                }

                if (response.isSuccessful()) {

                    progressBar.setVisibility(View.INVISIBLE);

                    name = response.body().getName();
                    weatherList = response.body().getWeather();
                    Weather weather = weatherList.get(0);
                    String mainWeather = weather.getMain();
                    String descriptionWeather = weather.getDescription();
                    Main main = response.body().getMain();
                    String temperature = main.getTemp().toString();
                    String pressure = main.getPressure().toString();
                    String humidity = main.getHumidity().toString();
                    String visibility = response.body().getVisibility().toString();

                    // getting the coordinates
                    Coord coord = response.body().getCoord();
                    latitude = coord.getLat().toString();
                    longitude = coord.getLon().toString();

                    // adding to list and updating shared preferences
                    coord.setLat(coord.getLat());
                    coord.setLon(coord.getLon());
                    coordList.add(coord);
                    updateSharedPreferences(coordList);


                    // Setting values after getting data
                    cityNameText.setText(name);
                    mainWeatherText.setText(mainWeather);
                    tempText.setText(temperature);
                    pressureText.setText(pressure);
                    humidityText.setText(humidity);
                    visibilityText.setText(visibility);
                    weatherDesc.setText(descriptionWeather);
                }


            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.d("onFailure", t.toString());
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LocationWeatherDetailActivity.this, "Location not found!", Toast.LENGTH_LONG).show();
                finish();
            }

        });
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


    private void updateSharedPreferences(List<Coord> coordList) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();

        String json = gson.toJson(coordList);
        editor.putString("MyLocationList", json);
        editor.commit();
    }

    @OnClick(R.id.locationIcon)
    public void launchMapActivity() {
        Log.d("LocationWeatherDetail", "latitude: " + latitude);
        Log.d("LocationWeatherDetail", "longitude: " + longitude);
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("Lat", latitude);
        intent.putExtra("Lon", longitude);
        intent.putExtra("City", name);
        startActivity(intent);
    }

}

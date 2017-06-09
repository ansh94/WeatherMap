package com.anshdeep.weathermap.api.service;

import com.anshdeep.weathermap.api.models.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ANSHDEEP on 09-06-2017.
 */

public interface WeatherServiceInterface {


    @GET("weather")
    Call<WeatherResponse> getWeather(@Query("q") String cityName, @Query("appid") String apiKey);
}

package com.example.climadavidver.presentation

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class WeatherResponse(
    val location: Location? = null,
    val current: Current? = null
)

data class Location(
    val name: String? = null,
    val region: String? = null,
    val country: String? = null
)

data class Current(
    val temp_c: Float? = 0f,
    val humidity: Int? = 0,
    val condition: Condition? = null
)

data class Condition(
    val text: String? = null,
    val icon: String? = null
)

data class ForecastResponse(
    val location: Location? = null,
    val forecast: Forecast? = null
)

data class Forecast(
    val forecastday: List<ForecastDay> = emptyList()
)

data class ForecastDay(
    val date: String? = null,
    val day: Day? = null
)

data class Day(
    val maxtemp_c: Float? = 0f,
    val mintemp_c: Float? = 0f,
    val avghumidity: Float? = 0f,
    val condition: Condition? = null
)

interface WeatherService {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): WeatherResponse

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("days") days: Int
    ): ForecastResponse
}

object RetrofitClient {
    val service: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}
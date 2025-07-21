package com.example.climadavidver.presentation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/*
* WeatherResponse
* WeatherDayData
* */

@Composable
fun WeatherTodayScreen(onViewWeekClick: () -> Unit, onViewDetailsClick: () -> Unit) {
    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    LaunchedEffect(Unit) {
        try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                val query = "${location.latitude},${location.longitude}"
                Log.d("WeatherDebug", "Coordenadas: Lat=${location.latitude}, Lon=${location.longitude}")
                weatherData = RetrofitClient.service.getCurrentWeather(
                    apiKey = Constants.API_KEY,
                    query = query
                )
                Log.d("WeatherDebug", "Ubicación: ${weatherData?.location?.name}, ${weatherData?.location?.region}")
            } else {
                errorMessage = "No se pudo obtener la ubicación"
                Log.e("WeatherError", "Ubicación nula")
            }
        } catch (e: SecurityException) {
            errorMessage = "Permisos de ubicación no concedidos"
            Log.e("WeatherError", "Permisos no concedidos: ${e.message}", e)
        } catch (e: Exception) {
            errorMessage = "Error al obtener el clima"
            Log.e("WeatherError", "Error al obtener el clima: ${e.message}", e)
        }
        isLoading = false
    }

    // Esto muestra el reloj al inicio de la pantalla
    Scaffold(timeText = { TimeText() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getWeatherGradient("current"))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.88f)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> LoadingIndicator()
                    errorMessage != null -> ErrorMessage(message = errorMessage!!)
                    weatherData != null -> CircularWeatherLayout(
                        data = weatherData!!,
                        locationName = weatherData?.location?.let { "${it.name ?: "Desconocido"}, ${it.region ?: ""}" } ?: "Ubicación desconocida",
                        onViewWeekClick = onViewWeekClick,
                        onViewDetailsClick = onViewDetailsClick
                    )
                    else -> ErrorMessage()
                }
            }
        }
    }
}

// Convierte los datos de la pantalla principal
// Fuera del Composable, o dentro de un objeto compañero si lo prefieres
fun convertCelsiusToFahrenheit(celsius: Float): Float {
    return celsius * 1.8f + 32f
}

@Composable
private fun CircularWeatherLayout(
    // Weather Response se encarga de devolver información del clima
    data: WeatherResponse,
    locationName: String,
    onViewWeekClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
) {
    // Cambiar Temperatura
    var showFahrenheit by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con ubicación y hora
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = locationName,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Actualizado ahora",
                    fontSize = 6.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Contenido central enriquecido
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                // modifier = Modifier.weight(1f)
            ) {
                // Icono del clima más prominente
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getWeatherIcon(data.current?.condition?.text ?: "Desconocido", data.current?.temp_c?.toFloat() ?: 0f),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = getWeatherIconColor(data.current?.condition?.text ?: "Desconocido", data.current?.temp_c?.toFloat() ?: 0f)
                    )
                }

                // Temperatura principal
                val currentTempCelsius = data.current?.temp_c?.toFloat() ?: 0.0f
                val displayTemp = if (showFahrenheit) {
                    convertCelsiusToFahrenheit(currentTempCelsius).toInt()
                } else {
                    currentTempCelsius.toInt()
                }
                val tempUnit = if (showFahrenheit) "°F" else "°C"

                Text(
                    text = "$displayTemp$tempUnit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Condición del clima
                Text(
                    text = data.current?.condition?.text ?: "Desconocido",
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // Información adicional en fila
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    // Sensación térmica
                    val feelsLikeCelsius = (data.current?.temp_c?.toFloat() ?: 0.0f) + 2 // Usar feelslike_c si está disponible
                    val displayFeelsLike = if (showFahrenheit) {
                        convertCelsiusToFahrenheit(feelsLikeCelsius).toInt()
                    } else {
                        feelsLikeCelsius.toInt()
                    }
                    val feelsLikeUnit = if (showFahrenheit) "°F" else "°C"

                    WeatherInfoItem(
                        icon = Icons.Default.Speed,
                        label = "ST",
                        value = "$displayFeelsLike$feelsLikeUnit"
                    )

                    // Humedad
                    WeatherInfoItem(
                        icon = Icons.Default.Water,
                        label = "Hum",
                        value = "${data.current?.humidity ?: 0}%"
                    )

                    // Viento (valor simulado si no está disponible)
                    WeatherInfoItem(
                        icon = Icons.Default.Navigation,
                        label = "Viento",
                        value = "12km/h"
                    )
                }
            }

            // Área inferior con preview y botones en fila
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                // Mini preview del mañana
                // Asumiendo que 28°C es un valor fijo de ejemplo, si viene de 'data', ajústalo
                val tomorrowTempCelsius = 28f // O data.forecast.forecastday[1].day.avgtemp_c?.toFloat() ?: 0.0f
                val displayTomorrowTemp = if (showFahrenheit) {
                    convertCelsiusToFahrenheit(tomorrowTempCelsius).toInt()
                } else {
                    tomorrowTempCelsius.toInt()
                }
                val tomorrowUnit = if (showFahrenheit) "°F" else "°C"

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Mañana:",
                            fontSize = 7.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.width(2.dp))

                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(8.dp),
                            tint = Color(0xFFFFA726)
                        )

                        Spacer(modifier = Modifier.width(2.dp))

                        Text(
                            text = "$displayTomorrowTemp$tomorrowUnit",
                            fontSize = 7.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Botones en una fila con tamaño reducido
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 2.dp)
                ) {
                    Button(
                        onClick = onViewWeekClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Pronóstico",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Botón de Detalles
                    Button(
                        onClick = onViewDetailsClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Detalles",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Esta es la Row que contiene el botón que quieres ajustar.
                Row(
                    horizontalArrangement = Arrangement.Center, // Cambiado de SpaceEvenly a Center
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 2.dp)
                ) {
                    Button(
                        onClick = { showFahrenheit = !showFahrenheit },
                        modifier = Modifier
                            .width(100.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Ver en °F",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = label,
            fontSize = 6.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 8.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Cargando...",
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun ErrorMessage(message: String = "Error de conexión") {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherListScreen(onBackClick: () -> Unit) {
    val listState = rememberScalingLazyListState()

    var forecastData by remember { mutableStateOf<List<WeatherDayData>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var locationName by remember { mutableStateOf("Obteniendo ubicación...") }
    var showFahrenheit by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    LaunchedEffect(Unit) {
        try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                val query = "${location.latitude},${location.longitude}"
                Log.d("WeatherDebug", "Coordenadas: Lat=${location.latitude}, Lon=${location.longitude}")
                val response = RetrofitClient.service.getForecast(
                    apiKey = Constants.API_KEY,
                    query = query,
                    days = 3
                )
                locationName = response.location?.name ?: "Ubicación desconocida"
                Log.d("ForecastDebug", "Ubicación obtenida: $locationName")
                forecastData = response.forecast?.forecastday?.map { forecastDay ->
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(forecastDay.date ?: "")
                    val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(date ?: Date())
                    WeatherDayData(
                        day = dayName,
                        condition = forecastDay.day?.condition?.text ?: "Desconocido",
                        temperature = forecastDay.day?.maxtemp_c?.toInt() ?: 0,
                        humidity = forecastDay.day?.avghumidity?.toInt() ?: 0
                    )
                } ?: emptyList()
            } else {
                errorMessage = "No se pudo obtener la ubicación"
                Log.e("ForecastError", "Ubicación nula")
            }
        } catch (e: SecurityException) {
            errorMessage = "Permisos de ubicación no concedidos"
            Log.e("ForecastError", "Permisos no concedidos: ${e.message}", e)
        } catch (e: Exception) {
            errorMessage = "Error al obtener el pronóstico"
            Log.e("ForecastError", "Error al obtener el pronóstico: ${e.message}", e)
        }
        isLoading = false
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getWeatherGradient("weekly"))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .align(Alignment.Center)
            ) {
                when {
                    isLoading -> LoadingIndicator()
                    errorMessage != null -> ErrorMessage(message = errorMessage!!)
                    forecastData != null -> ScalingLazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item { CircularHeader(
                            locationName = locationName,
                            onBackClick = onBackClick,
                            onToggleTemperatureFormat = { showFahrenheit = !showFahrenheit }
                        ) }
                        items(forecastData!!) { dayData -> CircularWeatherCard(dayData = dayData, showFahrenheit = showFahrenheit) }
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "• Fin del pronóstico •",
                                fontSize = 6.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> ErrorMessage()
                }
            }
        }
    }
}

@Composable
private fun CircularHeader(
    locationName: String,
    onBackClick: () -> Unit,
    onToggleTemperatureFormat: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = "Volver",
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pronóstico de 7 días",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = locationName,
                    fontSize = 6.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onToggleTemperatureFormat,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = "Cambiar Formato",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun CircularWeatherCard(
    dayData: WeatherDayData,
    showFahrenheit: Boolean
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(36.dp)
            .background(
                Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna del día
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    // Temperatura del Día
                    text = dayData.day.take(3),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    // Condición del Día
                    text = dayData.condition.take(6),
                    fontSize = 6.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Icono del clima
            Icon(
                imageVector = getWeatherIcon(dayData.condition, dayData.temperature.toFloat()),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = getWeatherIconColor(dayData.condition, dayData.temperature.toFloat())
            )

            // Aquí es donde usaremos el 'showFahrenheit' que nos pasaron
            val currentTempCelsius = dayData.temperature?.toFloat() ?: 0.0f
            val displayTemp = if (showFahrenheit) {
                convertCelsiusToFahrenheit(currentTempCelsius).toInt()
            } else {
                currentTempCelsius.toInt()
            }
            val tempUnit = if (showFahrenheit) "°F" else "°C"

            // Ajusta el cálculo de displayTempMin para que también use el formato correcto
            val displayTempMin = if (showFahrenheit) {
                convertCelsiusToFahrenheit(currentTempCelsius - 8).toInt()
            } else {
                (currentTempCelsius - 8).toInt()
            }

            // Columna de temperatura
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.8f)
            ) {
                Text(
                    // Temperatura del Día
                    text = "$displayTemp$tempUnit",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    // Temperatura mínima
                    text = "$displayTempMin$tempUnit",
                    fontSize = 7.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Humedad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.8f),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Water,
                    contentDescription = null,
                    modifier = Modifier.size(8.dp),
                    tint = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${dayData.humidity}%",
                    fontSize = 7.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun WeatherDetailScreen(onBackClick: () -> Unit) {
    Scaffold(timeText = { TimeText() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getWeatherGradient("details")),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxSize(0.8f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Text(
                        text = "Información Adicional",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Información adicional simulada
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        DetailInfoRow(
                            label = "Presión atmosférica",
                            value = "1013 hPa",
                            icon = Icons.Default.Speed
                        )

                        DetailInfoRow(
                            label = "Visibilidad",
                            value = "10 km",
                            icon = Icons.Default.Visibility
                        )

                        DetailInfoRow(
                            label = "Índice UV",
                            value = "Alto (7)",
                            icon = Icons.Default.WbSunny
                        )

                        DetailInfoRow(
                            label = "Amanecer",
                            value = "06:45",
                            icon = Icons.Default.WbTwilight
                        )

                        DetailInfoRow(
                            label = "Atardecer",
                            value = "19:30",
                            icon = Icons.Default.Brightness4
                        )
                    }

                    // Botón de regreso
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIos,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Volver",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 7.sp,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = value,
            fontSize = 8.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

// Funciones optimizadas para pantallas circulares con lógica de temperatura
private fun getWeatherIcon(condition: String, temperature: Float = 20.0f): ImageVector {
    return when {
        // Temperaturas muy altas (30°C+) - Priorizar sol aunque esté nublado
        temperature >= 30.0f -> {
            when {
                condition.contains("sun", ignoreCase = true) ||
                        condition.contains("clear", ignoreCase = true) ||
                        condition.contains("soleado", ignoreCase = true) -> Icons.Default.WbSunny

                condition.contains("rain", ignoreCase = true) ||
                        condition.contains("lluvia", ignoreCase = true) -> Icons.Default.Umbrella

                condition.contains("storm", ignoreCase = true) ||
                        condition.contains("thunder", ignoreCase = true) ||
                        condition.contains("tormenta", ignoreCase = true) -> Icons.Default.Bolt

                // Para temperaturas altas, usar sol parcialmente nublado en lugar de solo nubes
                else -> Icons.Default.WbSunny
            }
        }

        // Temperaturas bajas (15°C-) - Dar prioridad a condiciones frías
        temperature <= 15.0f -> {
            when {
                condition.contains("rain", ignoreCase = true) ||
                        condition.contains("lluvia", ignoreCase = true) -> Icons.Default.Umbrella

                condition.contains("storm", ignoreCase = true) ||
                        condition.contains("thunder", ignoreCase = true) ||
                        condition.contains("tormenta", ignoreCase = true) -> Icons.Default.Bolt

                condition.contains("cloud", ignoreCase = true) ||
                        condition.contains("nublado", ignoreCase = true) ||
                        condition.contains("overcast", ignoreCase = true) -> Icons.Default.Cloud

                condition.contains("sun", ignoreCase = true) ||
                        condition.contains("clear", ignoreCase = true) ||
                        condition.contains("soleado", ignoreCase = true) -> Icons.Default.WbSunny

                else -> Icons.Default.Cloud
            }
        }

        // Temperaturas normales - Lógica estándar
        else -> {
            when {
                condition.contains("sun", ignoreCase = true) ||
                        condition.contains("clear", ignoreCase = true) ||
                        condition.contains("soleado", ignoreCase = true) -> Icons.Default.WbSunny

                condition.contains("rain", ignoreCase = true) ||
                        condition.contains("lluvia", ignoreCase = true) -> Icons.Default.Umbrella

                condition.contains("storm", ignoreCase = true) ||
                        condition.contains("thunder", ignoreCase = true) ||
                        condition.contains("tormenta", ignoreCase = true) -> Icons.Default.Bolt

                condition.contains("cloud", ignoreCase = true) ||
                        condition.contains("nublado", ignoreCase = true) ||
                        condition.contains("overcast", ignoreCase = true) -> Icons.Default.Cloud

                condition.contains("partly", ignoreCase = true) ||
                        condition.contains("parcialmente", ignoreCase = true) -> Icons.Default.CloudQueue

                else -> Icons.Default.Cloud
            }
        }
    }
}

private fun getWeatherIconColor(condition: String, temperature: Float = 20.0f): Color {
    return when {
        // Temperaturas muy altas - Colores cálidos
        temperature >= 30.0f -> {
            when {
                condition.contains("rain", ignoreCase = true) ||
                        condition.contains("lluvia", ignoreCase = true) -> Color(0xFF42A5F5)

                condition.contains("storm", ignoreCase = true) ||
                        condition.contains("thunder", ignoreCase = true) ||
                        condition.contains("tormenta", ignoreCase = true) -> Color(0xFF7E57C2)

                // Para temperaturas altas, usar amarillo/naranja brillante
                else -> Color(0xFFFF8F00) // Naranja intenso para calor
            }
        }

        // Temperaturas bajas - Colores fríos
        temperature <= 15.0f -> {
            when {
                condition.contains("sun", ignoreCase = true) ||
                        condition.contains("clear", ignoreCase = true) ||
                        condition.contains("soleado", ignoreCase = true) -> Color(0xFFFFB74D) // Amarillo más suave

                condition.contains("rain", ignoreCase = true) ||
                        condition.contains("lluvia", ignoreCase = true) -> Color(0xFF1976D2) // Azul más frío

                condition.contains("storm", ignoreCase = true) ||
                        condition.contains("thunder", ignoreCase = true) ||
                        condition.contains("tormenta", ignoreCase = true) -> Color(0xFF7E57C2)

                else -> Color(0xFF78909C) // Gris azulado para frío
            }
        }

        // Temperaturas normales - Colores estándar
        else -> {
            when {
                condition.contains("sun", ignoreCase = true) ||
                        condition.contains("clear", ignoreCase = true) ||
                        condition.contains("soleado", ignoreCase = true) -> Color(0xFFFFA726)

                condition.contains("rain", ignoreCase = true) ||
                        condition.contains("lluvia", ignoreCase = true) -> Color(0xFF42A5F5)

                condition.contains("storm", ignoreCase = true) ||
                        condition.contains("thunder", ignoreCase = true) ||
                        condition.contains("tormenta", ignoreCase = true) -> Color(0xFF7E57C2)

                condition.contains("cloud", ignoreCase = true) ||
                        condition.contains("nublado", ignoreCase = true) ||
                        condition.contains("overcast", ignoreCase = true) -> Color(0xFF90A4AE)

                condition.contains("partly", ignoreCase = true) ||
                        condition.contains("parcialmente", ignoreCase = true) -> Color(0xFFFFB74D)

                else -> Color(0xFF90A4AE)
            }
        }
    }
}

private fun getWeatherGradient(screen: String): Brush {
    return when (screen) {
        "current" -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF0EA5E9),
                Color(0xFF3B82F6),
                Color(0xFF1E40AF)
            ),
            radius = 400f
        )
        "weekly" -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF1E40AF),
                Color(0xFF3730A3),
                Color(0xFF312E81)
            ),
            radius = 400f
        )
        "details" -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF3B82F6),
                Color(0xFF1E40AF)
            ),
            radius = 400f
        )
        else -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF0EA5E9),
                Color(0xFF3B82F6)
            ),
            radius = 400f
        )
    }
}

data class WeatherDayData(
    val day: String,
    val condition: String,
    val temperature: Int,
    val humidity: Int
)

private fun getWeeklyForecastData(): List<WeatherDayData> {
    return listOf(
        WeatherDayData("Lunes", "Soleado", 32, 55),
        WeatherDayData("Martes", "Nublado", 23, 75),
        WeatherDayData("Miércoles", "Lluvia", 19, 90),
        WeatherDayData("Jueves", "Parcialmente nublado", 25, 65),
        WeatherDayData("Viernes", "Soleado", 31, 50),
        WeatherDayData("Sábado", "Nublado", 21, 80),
        WeatherDayData("Domingo", "Soleado", 28, 60)
    )
}
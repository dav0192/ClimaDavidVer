/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.climadavidver.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.navigation.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        // Solicita los permisos de ubicación
        requestLocationPermission()
        // Función principal, que invoca la interfaz
        setContent {
            WearApp()
        }
    }

    // Solicitar la ubicación del usuario
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        }
    }
}

// fun WearApp(greetingName: String) {
@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "weather_today"
    ) {
        composable("weather_today") {
            WeatherTodayScreen(
                onViewWeekClick = {
                    navController.navigate("weather_list")
                },
                onViewDetailsClick = {
                    navController.navigate("weather_detail")
                }
            )
        }

        composable("weather_list") {
            WeatherListScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable("weather_detail") {
            WeatherDetailScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}
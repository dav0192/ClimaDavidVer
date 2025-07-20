plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.climadavidver"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.climadavidver"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.play.services.wearable)
    // Dependencias Generales de Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.androidx.material.icons.extended)

    // Dependencias de Compose para WearOS
    implementation(libs.wear.compose.material)

    // Foundation es un aditivo para que puedas usar la versión movil en tu app WearOS
    implementation(libs.wear.compose.foundation)

    // Anotaciones de vista previa para Compose para WearOS
    implementation(libs.androidx.compose.ui.tooling)

    implementation(libs.horologist.compose.layout)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(platform(libs.compose.bom))

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
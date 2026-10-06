plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // necesario para que Room genere su código (base de datos SQLite local)
    // Firebase necesita este plugin para leer el archivo google-services.json.
    id("com.google.gms.google-services") version "4.4.1" apply false
}

android {
    namespace = "com.sportapp.tiendasportmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sportapp.tiendasportmobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            // Mientras pruebas en el EMULADOR de Android Studio contra tu backend
            // local (el que corres con "mvn spring-boot:run" en tu computador).
            buildConfigField("String", "BASE_URL", "\"http://localhost:8081/api/\"")
        }
        release {
            isMinifyEnabled = false
            // IMPORTANTE: reemplaza esta URL por la que te dé Railway/Render
            // al desplegar el backend (termina en "/api/", con HTTPS).
            buildConfigField("String", "BASE_URL", "\"https://TU-BACKEND-EN-RAILWAY.up.railway.app/api/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // --- Básicos de Android / Kotlin ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- Tareas asíncronas (Corrutinas) — tecnología emergente vista en el material de formación ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- Retrofit: consumo de la API REST (el mismo backend Java del proyecto web) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Room (SQLite): guarda el carrito de compras localmente en el dispositivo ---
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    kapt("androidx.room:room-compiler:2.8.5")

    // --- Firebase: Analytics — tecnología emergente/disruptiva pedida en la guía ---
    // --- implementation(platform("com.google.firebase:firebase-bom:32.7.4")) ---
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    // --- implementation("com.google.firebase:firebase-analytics-ktx") ---
    implementation("com.google.firebase:firebase-analytics")

    // --- Imágenes desde URL (Glide) ---
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
}

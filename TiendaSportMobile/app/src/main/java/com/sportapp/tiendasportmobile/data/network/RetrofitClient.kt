package com.sportapp.tiendasportmobile.data.network

import com.sportapp.tiendasportmobile.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * La URL base ahora viene de BuildConfig.BASE_URL (definida en app/build.gradle.kts):
     * - Compilación "debug" (la que usas normalmente en el emulador mientras programas):
     *   apunta a 10.0.2.2, el alias especial que usa Android para llegar al backend
     *   que corres en TU COMPUTADOR (con "mvn spring-boot:run"), no a internet.
     * - Compilación "release" (la que genera el APK final para repartir o publicar):
     *   apunta a la URL real de tu backend en Railway/Render, con HTTPS.
     *
     * Así nunca tienes que acordarte de cambiar esto a mano antes de generar el APK final.
     */
    private val BASE_URL = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

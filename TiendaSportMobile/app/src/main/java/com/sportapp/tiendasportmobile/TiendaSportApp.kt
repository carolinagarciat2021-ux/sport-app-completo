package com.sportapp.tiendasportmobile

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

class TiendaSportApp : Application() {

    lateinit var firebaseAnalytics: FirebaseAnalytics
        private set

    override fun onCreate() {
        super.onCreate()
        // Tecnología emergente incorporada al proyecto: Firebase Analytics.
        // Registra eventos de uso real de la app (login, compras) sin necesidad
        // de programar nuestro propio sistema de métricas desde cero.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }
}

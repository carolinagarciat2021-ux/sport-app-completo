package com.sportapp.tiendasportmobile.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda el token de sesión y los datos del usuario logueado en el dispositivo,
 * igual que el frontend web los guarda en localStorage, para que la sesión
 * sobreviva a cerrar y volver a abrir la app.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tienda_sport_sesion", Context.MODE_PRIVATE)

    fun guardarSesion(token: String, idClientes: Int, nombre: String, correo: String, idRol: Int) {
        prefs.edit()
            .putString("token", token)
            .putInt("id_clientes", idClientes)
            .putString("nombre", nombre)
            .putString("correo", correo)
            .putInt("id_rol", idRol)
            .apply()
    }

    fun obtenerToken(): String? = prefs.getString("token", null)
    fun obtenerNombre(): String? = prefs.getString("nombre", null)
    fun obtenerIdRol(): Int = prefs.getInt("id_rol", 2)
    fun haySesionActiva(): Boolean = !obtenerToken().isNullOrEmpty()

    /** El header que exige el backend en cada endpoint protegido. */
    fun tokenConBearer(): String = "Bearer ${obtenerToken() ?: ""}"

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}

package com.sportapp.tiendasportmobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una fila del carrito, guardada en SQLite (a través de Room) directamente en
 * el dispositivo. Así el carrito de un cliente invitado sobrevive a cerrar la
 * app, igual que en la versión web (que lo guarda en localStorage del navegador).
 */
@Entity(tableName = "carrito_items")
data class CarritoItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idProducto: Int,
    val nombre: String,
    val precioMayorista: Double,
    val imagenUrl: String?,
    val talla: String,
    val color: String,
    var cantidad: Int,
    val stockDisponible: Int
)

package com.sportapp.tiendasportmobile.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito_items ORDER BY id ASC")
    fun observarCarrito(): Flow<List<CarritoItemEntity>>

    @Query("SELECT * FROM carrito_items WHERE idProducto = :idProducto AND talla = :talla AND color = :color LIMIT 1")
    suspend fun buscarItem(idProducto: Int, talla: String, color: String): CarritoItemEntity?

    @Insert
    suspend fun insertar(item: CarritoItemEntity)

    @Update
    suspend fun actualizar(item: CarritoItemEntity)

    @Delete
    suspend fun eliminar(item: CarritoItemEntity)

    @Query("DELETE FROM carrito_items")
    suspend fun vaciarCarrito()
}

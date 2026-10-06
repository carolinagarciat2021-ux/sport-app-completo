package com.sportapp.tiendasportmobile.data.repository

import android.content.Context
import com.sportapp.tiendasportmobile.data.local.AppDatabase
import com.sportapp.tiendasportmobile.data.local.CarritoItemEntity
import com.sportapp.tiendasportmobile.data.model.*
import com.sportapp.tiendasportmobile.data.network.RetrofitClient
import com.sportapp.tiendasportmobile.util.SessionManager
import kotlinx.coroutines.flow.Flow
import com.sportapp.tiendasportmobile.data.network.PedidoResponseItem

/**
 * Repositorio: es la única puerta de entrada para que las pantallas (Activities)
 * pidan datos, ya sea que vengan de la red (backend) o de la base de datos local
 * (carrito en SQLite). Así ninguna pantalla llama a Retrofit o a Room directamente,
 * siguiendo el mismo espíritu de separación de capas que ya usa el proyecto web
 * (api.js separado de los componentes de React).
 */
class TiendaRepository(context: Context) {

    private val api = RetrofitClient.api
    private val carritoDao = AppDatabase.obtener(context).carritoDao()
    val session = SessionManager(context)

    // ----------- Autenticación -----------
    suspend fun iniciarSesion(correo: String, password: String, rol: String): Result<LoginResponse> {
        val respuesta = api.iniciarSesion(LoginRequest(correo, password, rol))
        return if (respuesta.isSuccessful && respuesta.body() != null) {
            val body = respuesta.body()!!
            session.guardarSesion(body.token, body.id_clientes, body.nombre, body.correo, body.id_rol)
            Result.success(body)
        } else {
            Result.failure(Exception(extraerError(respuesta.errorBody()?.string())))
        }
    }

    suspend fun registrarCliente(cliente: Cliente): Result<Unit> {
        val respuesta = api.registrarCliente(cliente)
        return if (respuesta.isSuccessful) Result.success(Unit)
        else Result.failure(Exception(extraerError(respuesta.errorBody()?.string())))
    }

    // ----------- Catálogo -----------
    suspend fun listarProductos(): Result<List<Producto>> {
        val respuesta = api.listarProductos()
        return if (respuesta.isSuccessful) Result.success(respuesta.body() ?: emptyList())
        else Result.failure(Exception(extraerError(respuesta.errorBody()?.string())))
    }

    suspend fun crearProducto(producto: Producto): Result<String> {
        val r = api.crearProducto(session.tokenConBearer(), producto)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Producto creado")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    suspend fun actualizarProducto(id: Int, producto: Producto): Result<String> {
        val r = api.actualizarProducto(session.tokenConBearer(), id, producto)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Producto actualizado")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    suspend fun eliminarProducto(id: Int): Result<String> {
        val r = api.eliminarProducto(session.tokenConBearer(), id)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Producto eliminado")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }
    suspend fun listarCategorias(): Result<List<Categoria>> {
        val respuesta = api.listarCategorias()
        return if (respuesta.isSuccessful) Result.success(respuesta.body() ?: emptyList())
        else Result.failure(Exception(extraerError(respuesta.errorBody()?.string())))
    }

    suspend fun crearCategoria(categoria: Categoria): Result<String> {
        val r = api.crearCategoria(session.tokenConBearer(), categoria)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Categoría creada")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    suspend fun actualizarCategoria(id: Int, categoria: Categoria): Result<String> {
        val r = api.actualizarCategoria(session.tokenConBearer(), id, categoria)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Categoría actualizada")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    suspend fun eliminarCategoria(id: Int): Result<String> {
        val r = api.eliminarCategoria(session.tokenConBearer(), id)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Categoría eliminada")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    suspend fun listarUsuarios(): Result<List<UsuarioItem>> {
        val r = api.listarUsuarios(session.tokenConBearer())
        return if (r.isSuccessful) Result.success(r.body() ?: emptyList())
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    suspend fun actualizarUsuario(id: Int, datos: ActualizarUsuarioRequest): Result<String> {
        val r = api.actualizarUsuario(session.tokenConBearer(), id, datos)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Usuario actualizado")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    suspend fun eliminarUsuario(id: Int): Result<String> {
        val r = api.eliminarUsuario(session.tokenConBearer(), id)
        return if (r.isSuccessful) Result.success(r.body()?.mensaje ?: "Usuario eliminado")
        else Result.failure(Exception(extraerError(r.errorBody()?.string())))
    }

    // ----------- Carrito local (SQLite vía Room) -----------
    fun observarCarrito(): Flow<List<CarritoItemEntity>> = carritoDao.observarCarrito()

    suspend fun agregarAlCarrito(item: CarritoItemEntity) {
        val existente = carritoDao.buscarItem(item.idProducto, item.talla, item.color)
        if (existente != null) {
            existente.cantidad += item.cantidad
            carritoDao.actualizar(existente)
        } else {
            carritoDao.insertar(item)
        }
    }

    suspend fun actualizarCantidad(item: CarritoItemEntity, nuevaCantidad: Int) {
        carritoDao.actualizar(item.copy(cantidad = nuevaCantidad.coerceIn(1, item.stockDisponible)))
    }

    suspend fun quitarDelCarrito(item: CarritoItemEntity) = carritoDao.eliminar(item)

    suspend fun vaciarCarrito() = carritoDao.vaciarCarrito()

    // ----------- Pedido -----------
    suspend fun confirmarPedido(items: List<CarritoItemEntity>): Result<Int> {
        val tokenHeader = session.tokenConBearer()
        val respuestaPedido = api.crearPedido(tokenHeader, PedidoRequest("Pendiente"))
        if (!respuestaPedido.isSuccessful || respuestaPedido.body() == null) {
            return Result.failure(Exception(extraerError(respuestaPedido.errorBody()?.string())))
        }
        val idPedido = respuestaPedido.body()!!.id_pedido

        for (item in items) {
            val respuestaDetalle = api.crearDetallePedido(
                tokenHeader,
                DetallePedidoRequest(idPedido, item.idProducto, item.talla, item.color, item.cantidad, item.precioMayorista)
            )
            if (!respuestaDetalle.isSuccessful) {
                return Result.failure(Exception(extraerError(respuestaDetalle.errorBody()?.string())))
            }
        }
        vaciarCarrito()
        return Result.success(idPedido)
    }

    suspend fun listarPedidos(): Result<List<PedidoResponseItem>> {
        val respuesta = api.listarPedidos(session.tokenConBearer())
        return if (respuesta.isSuccessful) Result.success(respuesta.body() ?: emptyList())
        else Result.failure(Exception(extraerError(respuesta.errorBody()?.string())))
    }

    suspend fun listarDetallePedido(idPedido: Int): Result<List<DetallePedidoItem>> {
        val respuesta = api.listarDetallePedido(session.tokenConBearer(), idPedido)
        return if (respuesta.isSuccessful) Result.success(respuesta.body() ?: emptyList())
        else Result.failure(Exception(extraerError(respuesta.errorBody()?.string())))
    }

    private fun extraerError(cuerpoError: String?): String {
        if (cuerpoError.isNullOrBlank()) return "Error de conexión con el servidor"
        return try {
            val regex = "\"error\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            regex.find(cuerpoError)?.groupValues?.get(1) ?: cuerpoError
        } catch (e: Exception) {
            cuerpoError
        }
    }
}

package com.sportapp.tiendasportmobile.data.network

import com.sportapp.tiendasportmobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Define los mismos endpoints que ya usa el frontend web (api.js), consumiendo
 * el backend Java (ServidorApi.java) tal cual está, sin cambiarle nada.
 */
interface ApiService {

    @POST("login")
    suspend fun iniciarSesion(@Body body: LoginRequest): Response<LoginResponse>

    @POST("clientes")
    suspend fun registrarCliente(@Body body: Cliente): Response<MensajeApi>

    @GET("categorias")
    suspend fun listarCategorias(): Response<List<Categoria>>

    @POST("categorias")
    suspend fun crearCategoria(
        @Header("Authorization") token: String,
        @Body body: Categoria
    ): Response<MensajeApi>

    @PUT("categorias/{id}")
    suspend fun actualizarCategoria(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Categoria
    ): Response<MensajeApi>

    @DELETE("categorias/{id}")
    suspend fun eliminarCategoria(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MensajeApi>

    @GET("producto")
    suspend fun listarProductos(): Response<List<Producto>>

    @POST("producto")
    suspend fun crearProducto(
        @Header("Authorization") token: String,
        @Body body: Producto
    ): Response<MensajeApi>

    @PUT("producto/{id}")
    suspend fun actualizarProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Producto
    ): Response<MensajeApi>

    @DELETE("producto/{id}")
    suspend fun eliminarProducto(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MensajeApi>

    @POST("pedidos")
    suspend fun crearPedido(
        @Header("Authorization") token: String,
        @Body body: PedidoRequest
    ): Response<PedidoResponse>

    @POST("detalle_pedido")
    suspend fun crearDetallePedido(
        @Header("Authorization") token: String,
        @Body body: DetallePedidoRequest
    ): Response<MensajeApi>

    @GET("pedidos")
    suspend fun listarPedidos(@Header("Authorization") token: String): Response<List<PedidoResponseItem>>

    @GET("detalle_pedido")
    suspend fun listarDetallePedido(
        @Header("Authorization") token: String,
        @Query("id_pedido") idPedido: Int
    ): Response<List<DetallePedidoItem>>

    @GET("clientes")
    suspend fun listarUsuarios(
        @Header("Authorization") token: String
    ): Response<List<UsuarioItem>>

    @PUT("clientes/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: ActualizarUsuarioRequest
    ): Response<MensajeApi>

    @DELETE("clientes/{id}")
    suspend fun eliminarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MensajeApi>

}

data class PedidoResponseItem(
    val id_pedido: Int,
    val fecha: String,
    val estado: String,
    val id_cliente: Int
)

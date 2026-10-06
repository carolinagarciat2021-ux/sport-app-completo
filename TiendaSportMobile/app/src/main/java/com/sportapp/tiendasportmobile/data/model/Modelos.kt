package com.sportapp.tiendasportmobile.data.model

/**
 * Modelos de datos de la app. Los nombres de los campos coinciden EXACTAMENTE
 * con las claves del JSON que devuelve ServidorApi.java (el mismo backend del
 * proyecto web), para que Gson (usado por Retrofit) pueda mapearlos automáticamente.
 */

data class Cliente(
    val id_clientes: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val identificacion: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val correo: String = "",
    val password: String = "",
    val id_rol: Int = 2
)

data class LoginRequest(
    val correo: String,
    val password: String,
    val rol: String
)

data class LoginResponse(
    val id_clientes: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val id_rol: Int = 0,
    val token: String = ""
)

data class Categoria(
    val id_categoria: Int = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    val tipo_categoria: String? = null
)

data class Producto(
    val id_producto: Int = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    val talla: String = "",
    val color: String = "",
    val genero: String = "Unisex",
    val precio_mayorista: Double = 0.0,
    val costo_producto: Double = 0.0,
    val stock: Int = 0,
    val imagen_url: String? = null,
    val imagenes_por_color: String? = null,
    val id_categoria: Int = 0
) {
    /** Devuelve la URL correcta según el color elegido, o la imagen general si no hay una específica. */
    fun imagenParaColor(colorElegido: String): String? {
        if (!imagenes_por_color.isNullOrBlank()) {
            imagenes_por_color.split(";").forEach { par ->
                val partes = par.split("=")
                if (partes.size == 2 && partes[0].trim().equals(colorElegido.trim(), ignoreCase = true)) {
                    return partes[1].trim()
                }
            }
        }
        return imagen_url
    }

    fun listaTallas(): List<String> =
        talla.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("M") }

    fun listaColores(): List<String> =
        color.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("Único") }
}

data class PedidoRequest(val estado: String = "Pendiente")

data class PedidoResponse(
    val mensaje: String = "",
    val id_pedido: Int = 0
)

data class DetallePedidoRequest(
    val id_pedido: Int,
    val id_producto: Int,
    val talla: String,
    val color: String,
    val cantidad: Int,
    val precio_unitario: Double
)

data class MensajeApi(val mensaje: String? = null, val error: String? = null)


data class DetallePedidoItem(
    val id_detalle: Int = 0,
    val id_pedido: Int = 0,
    val id_producto: Int = 0,
    val talla: String? = null,
    val color: String? = null,
    val cantidad: Int = 0,
    val precio_unitario: Double = 0.0
) {
    fun subtotal(): Double = precio_unitario * cantidad
}

data class UsuarioItem(
    val id_clientes: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val identificacion: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val correo: String = "",
    val id_rol: Int = 0
) {
    fun nombreRol(): String = when (id_rol) {
        1 -> "Administrador"
        3 -> "Vendedor"
        else -> "Cliente"
    }
}

/** Solo se envían los campos que realmente cambiaron (los que queden en null, el backend los ignora). */
data class ActualizarUsuarioRequest(
    val nombre: String? = null,
    val apellido: String? = null,
    val identificacion: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val correo: String? = null,
    val password: String? = null,
    val id_rol: Int? = null
)

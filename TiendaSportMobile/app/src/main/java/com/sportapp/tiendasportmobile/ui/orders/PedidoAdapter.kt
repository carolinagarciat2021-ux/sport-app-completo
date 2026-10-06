package com.sportapp.tiendasportmobile.ui.orders

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.DetallePedidoItem
import com.sportapp.tiendasportmobile.data.network.PedidoResponseItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Una fila por pedido. Al tocar la cabecera, se expande mostrando el detalle
 * (se pide al backend la PRIMERA vez que se expande, luego queda en caché
 * en memoria para no repetir la llamada).
 *
 * @param esAdminOVendedor si es true, se muestra "id_cliente: X" junto a la fecha
 *        (Administrador y Vendedor ven pedidos de todos los clientes).
 * @param onNecesitaDetalle se llama la primera vez que se expande un pedido,
 *        para que la Activity pida el detalle al backend.
 */
class PedidoAdapter(
    private val esAdminOVendedor: Boolean,
    private val onNecesitaDetalle: suspend (idPedido: Int) -> List<DetallePedidoItem>
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    private var pedidos: List<PedidoResponseItem> = emptyList()
    private val expandido = mutableSetOf<Int>()
    private val detalleCache = mutableMapOf<Int, List<DetallePedidoItem>>()
    private val totalCache = mutableMapOf<Int, Double>()

    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    fun actualizarLista(nuevaLista: List<PedidoResponseItem>) {
        pedidos = nuevaLista
        notifyDataSetChanged()
    }

    /** Se llama desde la Activity cuando ya se calculó el total de un pedido (para pintarlo sin esperar a expandir). */
    fun fijarTotal(idPedido: Int, total: Double) {
        totalCache[idPedido] = total
        val index = pedidos.indexOfFirst { it.id_pedido == idPedido }
        if (index >= 0) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(vista)
    }

    override fun getItemCount(): Int = pedidos.size

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.tvNumero.text = "Pedido #${pedido.id_pedido}"
        holder.tvFechaCliente.text = if (esAdminOVendedor) {
            "${pedido.fecha.take(10)} — Cliente ID: ${pedido.id_cliente}"
        } else {
            pedido.fecha.take(16).replace("T", " ")
        }

        holder.tvEstado.text = pedido.estado
        val color = when (pedido.estado) {
            "Pagado" -> Color.parseColor("#2E7D32")
            "Enviado" -> Color.parseColor("#1565C0")
            else -> Color.parseColor("#F9A825") // Pendiente
        }
        // .mutate() es importante: sin esto, todas las filas recicladas comparten
        // el mismo drawable y cambiarle el color a una fila pintaría TODAS las demás igual.
        (holder.tvEstado.background.mutate() as GradientDrawable).setColor(color)

        val total = totalCache[pedido.id_pedido]
        holder.tvTotal.text = if (total != null) formatoMoneda.format(total) else "..."

        val estaExpandido = expandido.contains(pedido.id_pedido)
        holder.contenedorDetalle.visibility = if (estaExpandido) View.VISIBLE else View.GONE
        holder.ivFlecha.rotation = if (estaExpandido) 180f else 0f

        if (estaExpandido) pintarDetalle(holder, pedido.id_pedido)

        holder.filaCabecera.setOnClickListener {
            if (expandido.contains(pedido.id_pedido)) {
                expandido.remove(pedido.id_pedido)
            } else {
                expandido.add(pedido.id_pedido)
            }
            notifyItemChanged(position)
        }
    }

    private fun pintarDetalle(holder: PedidoViewHolder, idPedido: Int) {
        holder.contenedorDetalle.removeAllViews()
        val cache = detalleCache[idPedido]
        if (cache != null) {
            cache.forEach { holder.contenedorDetalle.addView(crearFilaDetalle(holder, it)) }
            return
        }

        // Aún no se ha cargado: pide el detalle y luego repinta
        CoroutineScope(Dispatchers.Main).launch {
            val detalle = onNecesitaDetalle(idPedido)
            detalleCache[idPedido] = detalle
            val total = detalle.sumOf { it.subtotal() }
            fijarTotal(idPedido, total)
            if (expandido.contains(idPedido)) {
                holder.contenedorDetalle.removeAllViews()
                detalle.forEach { holder.contenedorDetalle.addView(crearFilaDetalle(holder, it)) }
            }
        }
    }

    private fun crearFilaDetalle(holder: PedidoViewHolder, item: DetallePedidoItem): TextView {
        return TextView(holder.itemView.context).apply {
            text = "• ${item.cantidad}x Producto #${item.id_producto} (${item.talla ?: "-"}, ${item.color ?: "-"}) — ${formatoMoneda.format(item.subtotal())}"
            textSize = 13f
            setTextColor(Color.parseColor("#555555"))
            setPadding(0, 2, 0, 2)
        }
    }

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val filaCabecera: View = view.findViewById(R.id.filaCabecera)
        val tvNumero: TextView = view.findViewById(R.id.tvNumeroPedido)
        val tvFechaCliente: TextView = view.findViewById(R.id.tvFechaCliente)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoPedido)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalPedido)
        val ivFlecha: android.widget.ImageView = view.findViewById(R.id.ivFlecha)
        val contenedorDetalle: android.widget.LinearLayout = view.findViewById(R.id.contenedorDetalle)
    }
}

package com.sportapp.tiendasportmobile.ui.inventario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.Producto
import java.text.NumberFormat
import java.util.Locale

class InventarioAdapter(
    private val onEditar: (Producto) -> Unit,
    private val onEliminar: (Producto) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder>() {

    private var productos: List<Producto> = emptyList()
    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply { maximumFractionDigits = 0 }

    fun actualizarLista(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_inventario, parent, false)
        return InventarioViewHolder(vista)
    }

    override fun getItemCount(): Int = productos.size

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val p = productos[position]
        holder.tvNombre.text = p.nombre
        holder.tvDetalle.text = "Talla ${p.talla} · ${p.color} · Costo ${formatoMoneda.format(p.costo_producto)} · Mayorista ${formatoMoneda.format(p.precio_mayorista)}"
        holder.tvStock.text = "Stock actual: ${p.stock} uds."
        holder.tvStock.setTextColor(if (p.stock <= 0) android.graphics.Color.parseColor("#D32F2F") else android.graphics.Color.parseColor("#2E7D32"))

        holder.btnEditar.setOnClickListener { onEditar(p) }
        holder.btnEliminar.setOnClickListener { onEliminar(p) }
    }

    class InventarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreInv)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalleInv)
        val tvStock: TextView = view.findViewById(R.id.tvStockInv)
        val btnEditar: android.widget.Button = view.findViewById(R.id.btnEditarInv)
        val btnEliminar: android.widget.Button = view.findViewById(R.id.btnEliminarInv)
    }
}

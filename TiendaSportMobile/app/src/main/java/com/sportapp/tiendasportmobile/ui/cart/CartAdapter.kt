package com.sportapp.tiendasportmobile.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sportapp.tiendasportmobile.data.local.CarritoItemEntity
import com.sportapp.tiendasportmobile.databinding.ItemCarritoBinding
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onCambiarCantidad: (CarritoItemEntity, Int) -> Unit,
    private val onQuitar: (CarritoItemEntity) -> Unit
) : ListAdapter<CarritoItemEntity, CartAdapter.CartViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCarritoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CarritoItemEntity) {
            val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
            binding.tvNombre.text = item.nombre
            binding.tvTallaColor.text = "Talla: ${item.talla} · Color: ${item.color}"
            binding.tvSubtotal.text = "$${formato.format(item.precioMayorista * item.cantidad)}"
            binding.etCantidad.setText(item.cantidad.toString())

            Glide.with(binding.root.context).load(item.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivImagen)

            binding.btnQuitar.setOnClickListener { onQuitar(item) }
            binding.btnMas.setOnClickListener {
                if (item.cantidad < item.stockDisponible) onCambiarCantidad(item, item.cantidad + 1)
            }
            binding.btnMenos.setOnClickListener {
                if (item.cantidad > 1) onCambiarCantidad(item, item.cantidad - 1)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CarritoItemEntity>() {
        override fun areItemsTheSame(oldItem: CarritoItemEntity, newItem: CarritoItemEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CarritoItemEntity, newItem: CarritoItemEntity) = oldItem == newItem
    }
}

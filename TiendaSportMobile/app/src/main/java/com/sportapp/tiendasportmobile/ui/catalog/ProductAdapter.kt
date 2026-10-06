package com.sportapp.tiendasportmobile.ui.catalog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sportapp.tiendasportmobile.data.model.Producto
import com.sportapp.tiendasportmobile.databinding.ItemProductoBinding
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val onAgregarAlCarrito: (Producto) -> Unit
) : ListAdapter<Producto, ProductAdapter.ProductoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(producto: Producto) {
            val formatoPesos = NumberFormat.getNumberInstance(Locale("es", "CO"))
            binding.tvNombre.text = producto.nombre
            binding.tvPrecio.text = "$${formatoPesos.format(producto.precio_mayorista)}"
            binding.tvGeneroCategoria.text = "${producto.genero} · Stock: ${producto.stock}"

            Glide.with(binding.root.context)
                .load(producto.imagen_url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivImagen)

            binding.btnAgregar.isEnabled = producto.stock > 0
            binding.btnAgregar.text = if (producto.stock > 0) "Agregar al carrito" else "Sin stock"
            binding.btnAgregar.setOnClickListener { onAgregarAlCarrito(producto) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto) = oldItem.id_producto == newItem.id_producto
        override fun areContentsTheSame(oldItem: Producto, newItem: Producto) = oldItem == newItem
    }
}

package com.sportapp.tiendasportmobile.ui.categorias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.Categoria

class CategoriaAdapter(
    private val onEditar: (Categoria) -> Unit,
    private val onEliminar: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    private var categorias: List<Categoria> = emptyList()

    fun actualizarLista(nuevaLista: List<Categoria>) {
        categorias = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(vista)
    }

    override fun getItemCount(): Int = categorias.size

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val c = categorias[position]
        holder.tvNombre.text = c.nombre
        holder.tvDescripcion.text = c.descripcion ?: ""
        holder.btnEditar.setOnClickListener { onEditar(c) }
        holder.btnEliminar.setOnClickListener { onEliminar(c) }
    }

    class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCat)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionCat)
        val btnEditar: android.widget.Button = view.findViewById(R.id.btnEditarCat)
        val btnEliminar: android.widget.Button = view.findViewById(R.id.btnEliminarCat)
    }
}

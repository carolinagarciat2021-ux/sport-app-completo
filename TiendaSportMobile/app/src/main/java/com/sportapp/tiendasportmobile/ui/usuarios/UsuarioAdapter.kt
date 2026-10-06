package com.sportapp.tiendasportmobile.ui.usuarios

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.UsuarioItem

class UsuarioAdapter(
    private val onEditar: (UsuarioItem) -> Unit,
    private val onEliminar: (UsuarioItem) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    private var usuarios: List<UsuarioItem> = emptyList()

    fun actualizarLista(nuevaLista: List<UsuarioItem>) {
        usuarios = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(vista)
    }

    override fun getItemCount(): Int = usuarios.size

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val u = usuarios[position]
        holder.tvNombre.text = "${u.nombre} ${u.apellido}"
        holder.tvCorreo.text = u.correo
        holder.tvDetalle.text = "CC ${u.identificacion} · ${u.telefono} · ${u.direccion}"
        holder.tvRol.text = u.nombreRol()

        val color = when (u.id_rol) {
            1 -> Color.parseColor("#D32F2F") // Administrador
            3 -> Color.parseColor("#1565C0") // Vendedor
            else -> Color.parseColor("#2E7D32") // Cliente
        }
        (holder.tvRol.background.mutate() as GradientDrawable).setColor(color)

        holder.btnEditar.setOnClickListener { onEditar(u) }
        holder.btnEliminar.setOnClickListener { onEliminar(u) }
    }

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsu)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoUsu)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalleUsu)
        val tvRol: TextView = view.findViewById(R.id.tvRolUsu)
        val btnEditar: android.widget.Button = view.findViewById(R.id.btnEditarUsu)
        val btnEliminar: android.widget.Button = view.findViewById(R.id.btnEliminarUsu)
    }
}

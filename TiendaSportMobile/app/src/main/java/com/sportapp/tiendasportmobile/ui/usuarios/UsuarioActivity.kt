package com.sportapp.tiendasportmobile.ui.usuarios

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.ActualizarUsuarioRequest
import com.sportapp.tiendasportmobile.data.model.UsuarioItem
import com.sportapp.tiendasportmobile.ui.base.BaseDrawerActivity
import kotlinx.coroutines.launch

class UsuarioActivity : BaseDrawerActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private val nombresRoles = listOf("Cliente", "Vendedor", "Administrador")
    private val idsRoles = listOf(2, 3, 1) // mismo orden que nombresRoles

    override fun layoutRecursoPropio(): Int = R.layout.activity_usuarios
    override fun tituloPantalla(): String = "Gestión de Usuarios"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        swipeRefresh = findViewById(R.id.swipeRefreshUsuarios)
        recycler = findViewById(R.id.recyclerUsuarios)

        adapter = UsuarioAdapter(
            onEditar = { usuario -> abrirDialogoEditar(usuario) },
            onEliminar = { usuario -> confirmarEliminar(usuario) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        swipeRefresh.setOnRefreshListener { cargarUsuarios() }
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val resultado = repository.listarUsuarios()
            swipeRefresh.isRefreshing = false
            resultado.onSuccess { adapter.actualizarLista(it) }
                .onFailure { Toast.makeText(this@UsuarioActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun abrirDialogoEditar(usuario: UsuarioItem) {
        val vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_editar_usuario, null)
        val etNombre = vistaDialogo.findViewById<EditText>(R.id.etNombreDialog)
        val etApellido = vistaDialogo.findViewById<EditText>(R.id.etApellidoDialog)
        val etCorreo = vistaDialogo.findViewById<EditText>(R.id.etCorreoDialog)
        val etIdentificacion = vistaDialogo.findViewById<EditText>(R.id.etIdentificacionDialog)
        val etTelefono = vistaDialogo.findViewById<EditText>(R.id.etTelefonoDialog)
        val etDireccion = vistaDialogo.findViewById<EditText>(R.id.etDireccionDialog)
        val etPassword = vistaDialogo.findViewById<EditText>(R.id.etPasswordDialog)
        val spinnerRol = vistaDialogo.findViewById<Spinner>(R.id.spinnerRolDialog)

        etNombre.setText(usuario.nombre)
        etApellido.setText(usuario.apellido)
        etCorreo.setText(usuario.correo)
        etIdentificacion.setText(usuario.identificacion)
        etTelefono.setText(usuario.telefono)
        etDireccion.setText(usuario.direccion)

        spinnerRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombresRoles)
        spinnerRol.setSelection(idsRoles.indexOf(usuario.id_rol).coerceAtLeast(0))

        AlertDialog.Builder(this)
            .setTitle("Editar: ${usuario.nombre}")
            .setView(vistaDialogo)
            .setPositiveButton("Guardar") { _, _ ->
                val datos = ActualizarUsuarioRequest(
                    nombre = etNombre.text.toString().trim(),
                    apellido = etApellido.text.toString().trim(),
                    correo = etCorreo.text.toString().trim(),
                    identificacion = etIdentificacion.text.toString().trim(),
                    telefono = etTelefono.text.toString().trim(),
                    direccion = etDireccion.text.toString().trim(),
                    password = etPassword.text.toString().trim().ifEmpty { null },
                    id_rol = idsRoles[spinnerRol.selectedItemPosition]
                )
                lifecycleScope.launch {
                    val resultado = repository.actualizarUsuario(usuario.id_clientes, datos)
                    resultado.onSuccess {
                        Toast.makeText(this@UsuarioActivity, it, Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                    }.onFailure {
                        Toast.makeText(this@UsuarioActivity, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminar(usuario: UsuarioItem) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar usuario?")
            .setMessage("Vas a eliminar a \"${usuario.nombre} ${usuario.apellido}\" permanentemente.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    val resultado = repository.eliminarUsuario(usuario.id_clientes)
                    resultado.onSuccess {
                        Toast.makeText(this@UsuarioActivity, it, Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                    }.onFailure {
                        Toast.makeText(this@UsuarioActivity, "No se pudo eliminar: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

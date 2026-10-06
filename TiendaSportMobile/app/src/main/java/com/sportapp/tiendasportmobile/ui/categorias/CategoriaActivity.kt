package com.sportapp.tiendasportmobile.ui.categorias

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.Categoria
import com.sportapp.tiendasportmobile.ui.base.BaseDrawerActivity
import kotlinx.coroutines.launch

class CategoriaActivity : BaseDrawerActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var tvTituloForm: TextView
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var adapter: CategoriaAdapter

    /** null = modo "crear"; con valor = modo "editar" esa categoría. */
    private var categoriaEnEdicion: Categoria? = null

    override fun layoutRecursoPropio(): Int = R.layout.activity_categorias
    override fun tituloPantalla(): String = "Gestión de Categorías"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        swipeRefresh = findViewById(R.id.swipeRefreshCategorias)
        recycler = findViewById(R.id.recyclerCategorias)
        tvTituloForm = findViewById(R.id.tvTituloFormCategoria)
        etNombre = findViewById(R.id.etNombreCategoria)
        etDescripcion = findViewById(R.id.etDescripcionCategoria)
        btnGuardar = findViewById(R.id.btnGuardarCategoria)
        btnCancelar = findViewById(R.id.btnCancelarEdicionCategoria)

        adapter = CategoriaAdapter(
            onEditar = { categoria -> entrarEnModoEdicion(categoria) },
            onEliminar = { categoria -> confirmarEliminar(categoria) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnGuardar.setOnClickListener { guardar() }
        btnCancelar.setOnClickListener { salirDeModoEdicion() }
        swipeRefresh.setOnRefreshListener { cargarCategorias() }

        cargarCategorias()
    }

    private fun cargarCategorias() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val resultado = repository.listarCategorias()
            swipeRefresh.isRefreshing = false
            resultado.onSuccess { adapter.actualizarLista(it) }
                .onFailure { Toast.makeText(this@CategoriaActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun entrarEnModoEdicion(categoria: Categoria) {
        categoriaEnEdicion = categoria
        tvTituloForm.text = "Editando: ${categoria.nombre}"
        etNombre.setText(categoria.nombre)
        etDescripcion.setText(categoria.descripcion ?: "")
        btnGuardar.text = "Actualizar"
        btnCancelar.visibility = android.view.View.VISIBLE
    }

    private fun salirDeModoEdicion() {
        categoriaEnEdicion = null
        tvTituloForm.text = "Nueva Categoría"
        etNombre.setText("")
        etDescripcion.setText("")
        btnGuardar.text = "Crear"
        btnCancelar.visibility = android.view.View.GONE
    }

    private fun guardar() {
        val nombre = etNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre de la categoría es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }
        val categoria = Categoria(nombre = nombre, descripcion = etDescripcion.text.toString().trim())

        lifecycleScope.launch {
            val enEdicion = categoriaEnEdicion
            val resultado = if (enEdicion == null) repository.crearCategoria(categoria)
            else repository.actualizarCategoria(enEdicion.id_categoria, categoria)

            resultado.onSuccess {
                Toast.makeText(this@CategoriaActivity, it, Toast.LENGTH_SHORT).show()
                salirDeModoEdicion()
                cargarCategorias()
            }.onFailure {
                Toast.makeText(this@CategoriaActivity, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmarEliminar(categoria: Categoria) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar categoría?")
            .setMessage("Vas a eliminar \"${categoria.nombre}\". Si hay productos usando esta categoría, revisa antes de borrarla.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    val resultado = repository.eliminarCategoria(categoria.id_categoria)
                    resultado.onSuccess {
                        Toast.makeText(this@CategoriaActivity, it, Toast.LENGTH_SHORT).show()
                        cargarCategorias()
                    }.onFailure {
                        Toast.makeText(this@CategoriaActivity, "No se pudo eliminar: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

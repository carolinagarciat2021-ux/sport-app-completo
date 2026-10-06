package com.sportapp.tiendasportmobile.ui.inventario

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.Producto
import com.sportapp.tiendasportmobile.ui.base.BaseDrawerActivity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class InventarioActivity : BaseDrawerActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var tvValoracionCosto: TextView
    private lateinit var tvValoracionMayorista: TextView
    private lateinit var adapter: InventarioAdapter
    private val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply { maximumFractionDigits = 0 }

    override fun layoutRecursoPropio(): Int = R.layout.activity_inventario
    override fun tituloPantalla(): String = "Inventario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        swipeRefresh = findViewById(R.id.swipeRefreshInventario)
        recycler = findViewById(R.id.recyclerInventario)
        tvValoracionCosto = findViewById(R.id.tvValoracionCosto)
        tvValoracionMayorista = findViewById(R.id.tvValoracionMayorista)

        adapter = InventarioAdapter(
            onEditar = { producto ->
                val intent = Intent(this, ProductoFormActivity::class.java)
                intent.putExtra(ProductoFormActivity.EXTRA_ID_PRODUCTO, producto.id_producto)
                startActivity(intent)
            },
            onEliminar = { producto -> confirmarEliminar(producto) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAgregarProducto).setOnClickListener {
            startActivity(Intent(this, ProductoFormActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener { cargarInventario() }
    }

    override fun onResume() {
        super.onResume()
        // Recarga cada vez que vuelves (por ejemplo, después de crear/editar un producto)
        cargarInventario()
    }

    private fun cargarInventario() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val resultado = repository.listarProductos()
            swipeRefresh.isRefreshing = false
            resultado.onSuccess { lista ->
                adapter.actualizarLista(lista)
                pintarValoracion(lista)
            }.onFailure {
                android.widget.Toast.makeText(this@InventarioActivity, "Error: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pintarValoracion(lista: List<Producto>) {
        val totalCosto = lista.sumOf { it.costo_producto * it.stock }
        val totalMayorista = lista.sumOf { it.precio_mayorista * it.stock }
        tvValoracionCosto.text = formatoMoneda.format(totalCosto)
        tvValoracionMayorista.text = formatoMoneda.format(totalMayorista)
    }

    private fun confirmarEliminar(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar producto?")
            .setMessage("Vas a eliminar \"${producto.nombre}\" permanentemente. Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    val resultado = repository.eliminarProducto(producto.id_producto)
                    resultado.onSuccess {
                        android.widget.Toast.makeText(this@InventarioActivity, it, android.widget.Toast.LENGTH_SHORT).show()
                        cargarInventario()
                    }.onFailure {
                        android.widget.Toast.makeText(this@InventarioActivity, "No se pudo eliminar: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

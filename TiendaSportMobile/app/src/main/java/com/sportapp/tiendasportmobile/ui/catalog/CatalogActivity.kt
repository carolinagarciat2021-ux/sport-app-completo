package com.sportapp.tiendasportmobile.ui.catalog

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.local.CarritoItemEntity
import com.sportapp.tiendasportmobile.data.model.Producto
import com.sportapp.tiendasportmobile.databinding.ActivityCatalogBinding
import com.sportapp.tiendasportmobile.databinding.DialogAgregarCarritoBinding
import com.sportapp.tiendasportmobile.ui.base.BaseDrawerActivity
import com.sportapp.tiendasportmobile.ui.cart.CartActivity
import com.sportapp.tiendasportmobile.ui.login.LoginActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// CAMBIO 1: ahora hereda de BaseDrawerActivity en vez de AppCompatActivity,
// así obtiene el menú lateral (drawer) automáticamente.
class CatalogActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityCatalogBinding

    private lateinit var adapter: ProductAdapter

    private var listaCompleta: List<Producto> = emptyList()
    private var generoSeleccionado: String = "Todos"

    // CAMBIO 2: estos dos métodos son los que pide BaseDrawerActivity — le indican
    // cuál es el layout propio de esta pantalla y qué título mostrar en la barra de arriba.
    override fun layoutRecursoPropio(): Int = R.layout.activity_catalog
    override fun tituloPantalla(): String = "Catálogo — Tienda Sport"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CAMBIO 3: antes aquí había "binding = ActivityCatalogBinding.inflate(layoutInflater)"
        // más "setContentView(binding.root)". Ya no hace falta: BaseDrawerActivity ya insertó
        // el layout de esta pantalla dentro del drawer, así que solo lo "enlazamos" (bind).
        val vistaRaiz = findViewById<android.widget.FrameLayout>(R.id.contenidoFrame).getChildAt(0)
        binding = ActivityCatalogBinding.bind(vistaRaiz)

        adapter = ProductAdapter { producto -> mostrarDialogoAgregar(producto) }
        binding.recyclerProductos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerProductos.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { cargarProductos() }

        configurarFiltroGenero()
        cargarProductos()
        observarCarritoParaBadge()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_carrito -> {
                startActivity(Intent(this, CartActivity::class.java)); true
            }
            R.id.action_cerrar_sesion -> {
                repository.session.cerrarSesion()
                startActivity(Intent(this, LoginActivity::class.java))
                finish(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configurarFiltroGenero() {
        val opciones = listOf("Todos", "Hombre", "Mujer", "Infantil", "Unisex")
        binding.spinnerGenero.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        binding.spinnerGenero.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                generoSeleccionado = opciones[position]
                aplicarFiltro()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun aplicarFiltro() {
        val filtrada = if (generoSeleccionado == "Todos") listaCompleta
        else listaCompleta.filter { it.genero.equals(generoSeleccionado, ignoreCase = true) }
        adapter.submitList(filtrada)
        binding.tvVacio.visibility = if (filtrada.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun cargarProductos() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val resultado = repository.listarProductos()
            binding.swipeRefresh.isRefreshing = false
            resultado.onSuccess {
                listaCompleta = it
                aplicarFiltro()
            }.onFailure {
                Toast.makeText(this@CatalogActivity, "No se pudo cargar el catálogo: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarDialogoAgregar(producto: Producto) {
        val dialogBinding = DialogAgregarCarritoBinding.inflate(layoutInflater)
        dialogBinding.tvNombreProducto.text = producto.nombre

        val tallas = producto.listaTallas()
        val colores = producto.listaColores()
        dialogBinding.spinnerTalla.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tallas)
        dialogBinding.spinnerColor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colores)

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val tallaElegida = dialogBinding.spinnerTalla.selectedItem?.toString() ?: tallas.first()
                val colorElegido = dialogBinding.spinnerColor.selectedItem?.toString() ?: colores.first()
                val cantidad = dialogBinding.etCantidad.text.toString().toIntOrNull()?.coerceAtLeast(1) ?: 1

                lifecycleScope.launch {
                    repository.agregarAlCarrito(
                        CarritoItemEntity(
                            idProducto = producto.id_producto,
                            nombre = producto.nombre,
                            precioMayorista = producto.precio_mayorista,
                            imagenUrl = producto.imagenParaColor(colorElegido),
                            talla = tallaElegida,
                            color = colorElegido,
                            cantidad = cantidad,
                            stockDisponible = producto.stock
                        )
                    )
                    Toast.makeText(this@CatalogActivity, "Agregado: ${producto.nombre} ($tallaElegida, $colorElegido)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observarCarritoParaBadge() {
        lifecycleScope.launch {
            repository.observarCarrito().collectLatest { items ->
                val total = items.sumOf { it.cantidad }
                supportActionBar?.subtitle = if (total > 0) "🛒 $total en el carrito" else null
            }
        }
    }
}

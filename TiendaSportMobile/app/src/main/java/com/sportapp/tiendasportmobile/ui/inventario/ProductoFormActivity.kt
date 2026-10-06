package com.sportapp.tiendasportmobile.ui.inventario

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.Categoria
import com.sportapp.tiendasportmobile.data.model.Producto
import com.sportapp.tiendasportmobile.data.repository.TiendaRepository
import kotlinx.coroutines.launch

/**
 * Un solo formulario sirve tanto para "Registrar Producto" (sin EXTRA_ID_PRODUCTO,
 * todos los campos vacíos) como para "Editar" desde Inventario (con
 * EXTRA_ID_PRODUCTO, precarga los datos de ese producto).
 */
class ProductoFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_PRODUCTO = "extra_id_producto"
    }

    private lateinit var repository: TiendaRepository
    private var idProductoEditar: Int = -1
    private var categorias: List<Categoria> = emptyList()

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var spinnerGenero: Spinner
    private lateinit var spinnerCategoria: Spinner
    private lateinit var cbXS: CheckBox
    private lateinit var cbS: CheckBox
    private lateinit var cbM: CheckBox
    private lateinit var cbL: CheckBox
    private lateinit var cbXL: CheckBox
    private lateinit var cbXXL: CheckBox
    private lateinit var etColores: EditText
    private lateinit var etCosto: EditText
    private lateinit var etPrecioMayorista: EditText
    private lateinit var etStock: EditText
    private lateinit var etImagenUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_form)
        repository = TiendaRepository(this)
        idProductoEditar = intent.getIntExtra(EXTRA_ID_PRODUCTO, -1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = if (idProductoEditar == -1) "Registrar Producto" else "Editar Producto"

        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        spinnerGenero = findViewById(R.id.spinnerGeneroForm)
        spinnerCategoria = findViewById(R.id.spinnerCategoriaForm)
        cbXS = findViewById(R.id.cbXS); cbS = findViewById(R.id.cbS); cbM = findViewById(R.id.cbM)
        cbL = findViewById(R.id.cbL); cbXL = findViewById(R.id.cbXL); cbXXL = findViewById(R.id.cbXXL)
        etColores = findViewById(R.id.etColores)
        etCosto = findViewById(R.id.etCosto)
        etPrecioMayorista = findViewById(R.id.etPrecioMayorista)
        etStock = findViewById(R.id.etStock)
        etImagenUrl = findViewById(R.id.etImagenUrl)

        val generos = listOf("Unisex", "Hombre", "Mujer", "Infantil")
        spinnerGenero.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, generos)

        cargarCategoriasYLuegoProducto()

        findViewById<Button>(R.id.btnGuardarProducto).setOnClickListener { guardar() }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun cargarCategoriasYLuegoProducto() {
        lifecycleScope.launch {
            val resultado = repository.listarCategorias()
            resultado.onSuccess { lista ->
                categorias = lista
                spinnerCategoria.adapter = ArrayAdapter(this@ProductoFormActivity, android.R.layout.simple_spinner_dropdown_item, lista.map { it.nombre })
                if (idProductoEditar != -1) precargarProductoExistente()
            }.onFailure {
                Toast.makeText(this@ProductoFormActivity, "No se pudieron cargar las categorías: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** No existe un endpoint "traer 1 producto", así que buscamos en la lista completa por id. */
    private fun precargarProductoExistente() {
        lifecycleScope.launch {
            val resultado = repository.listarProductos()
            resultado.onSuccess { lista ->
                val p = lista.find { it.id_producto == idProductoEditar } ?: return@onSuccess
                etNombre.setText(p.nombre)
                etDescripcion.setText(p.descripcion ?: "")
                etColores.setText(p.color)
                etCosto.setText(p.costo_producto.toString())
                etPrecioMayorista.setText(p.precio_mayorista.toString())
                etStock.setText(p.stock.toString())
                etImagenUrl.setText(p.imagen_url ?: "")

                val generos = listOf("Unisex", "Hombre", "Mujer", "Infantil")
                spinnerGenero.setSelection(generos.indexOf(p.genero).coerceAtLeast(0))
                val indexCategoria = categorias.indexOfFirst { it.id_categoria == p.id_categoria }
                if (indexCategoria >= 0) spinnerCategoria.setSelection(indexCategoria)

                val tallasSeleccionadas = p.listaTallas()
                cbXS.isChecked = tallasSeleccionadas.contains("XS")
                cbS.isChecked = tallasSeleccionadas.contains("S")
                cbM.isChecked = tallasSeleccionadas.contains("M")
                cbL.isChecked = tallasSeleccionadas.contains("L")
                cbXL.isChecked = tallasSeleccionadas.contains("XL")
                cbXXL.isChecked = tallasSeleccionadas.contains("XXL")
            }
        }
    }

    private fun guardar() {
        val nombre = etNombre.text.toString().trim()
        val tallas = listOfNotNull(
            "XS".takeIf { cbXS.isChecked }, "S".takeIf { cbS.isChecked }, "M".takeIf { cbM.isChecked },
            "L".takeIf { cbL.isChecked }, "XL".takeIf { cbXL.isChecked }, "XXL".takeIf { cbXXL.isChecked }
        ).joinToString(", ")
        val costo = etCosto.text.toString().toDoubleOrNull()
        val precioMayorista = etPrecioMayorista.text.toString().toDoubleOrNull()
        val stock = etStock.text.toString().toIntOrNull()

        if (nombre.isEmpty() || tallas.isEmpty() || costo == null || precioMayorista == null || stock == null) {
            Toast.makeText(this, "Completa los campos obligatorios (*): nombre, tallas, costo, precio mayorista y stock", Toast.LENGTH_LONG).show()
            return
        }
        if (categorias.isEmpty()) {
            Toast.makeText(this, "Espera a que carguen las categorías e intenta de nuevo", Toast.LENGTH_SHORT).show()
            return
        }

        val categoriaElegida = categorias[spinnerCategoria.selectedItemPosition]
        val producto = Producto(
            nombre = nombre,
            descripcion = etDescripcion.text.toString().trim(),
            talla = tallas,
            color = etColores.text.toString().trim(),
            genero = spinnerGenero.selectedItem.toString(),
            precio_mayorista = precioMayorista,
            costo_producto = costo,
            stock = stock,
            imagen_url = etImagenUrl.text.toString().trim().ifEmpty { null },
            id_categoria = categoriaElegida.id_categoria
        )

        lifecycleScope.launch {
            val resultado = if (idProductoEditar == -1) repository.crearProducto(producto)
            else repository.actualizarProducto(idProductoEditar, producto)

            resultado.onSuccess {
                Toast.makeText(this@ProductoFormActivity, it, Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this@ProductoFormActivity, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

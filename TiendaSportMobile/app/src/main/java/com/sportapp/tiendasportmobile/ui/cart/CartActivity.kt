package com.sportapp.tiendasportmobile.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sportapp.tiendasportmobile.data.local.CarritoItemEntity
import com.sportapp.tiendasportmobile.data.repository.TiendaRepository
import com.sportapp.tiendasportmobile.databinding.ActivityCartBinding
import com.sportapp.tiendasportmobile.ui.login.LoginActivity
import com.sportapp.tiendasportmobile.ui.orders.OrderConfirmationActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var repository: TiendaRepository
    private lateinit var adapter: CartAdapter
    private var itemsActuales: List<CarritoItemEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = TiendaRepository(this)
        supportActionBar?.title = "Mi Carrito"

        adapter = CartAdapter(
            onCambiarCantidad = { item, nuevaCantidad ->
                lifecycleScope.launch { repository.actualizarCantidad(item, nuevaCantidad) }
            },
            onQuitar = { item ->
                lifecycleScope.launch { repository.quitarDelCarrito(item) }
            }
        )
        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = adapter

        binding.btnConfirmarPedido.setOnClickListener { confirmarPedido() }

        observarCarrito()
    }

    private fun observarCarrito() {
        lifecycleScope.launch {
            repository.observarCarrito().collectLatest { items ->
                itemsActuales = items
                adapter.submitList(items)

                val vacio = items.isEmpty()
                binding.tvCarritoVacio.visibility = if (vacio) android.view.View.VISIBLE else android.view.View.GONE
                binding.recyclerCarrito.visibility = if (vacio) android.view.View.GONE else android.view.View.VISIBLE
                binding.layoutTotal.visibility = if (vacio) android.view.View.GONE else android.view.View.VISIBLE

                val total = items.sumOf { it.precioMayorista * it.cantidad }
                val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
                binding.tvTotal.text = "Total: $${formato.format(total)}"
            }
        }
    }

    private fun confirmarPedido() {
        if (!repository.session.haySesionActiva()) {
            Toast.makeText(this, "Debes iniciar sesión para confirmar el pedido", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }
        if (itemsActuales.isEmpty()) return

        binding.btnConfirmarPedido.isEnabled = false
        lifecycleScope.launch {
            val resultado = repository.confirmarPedido(itemsActuales)
            binding.btnConfirmarPedido.isEnabled = true

            resultado.onSuccess { idPedido ->
                val intent = Intent(this@CartActivity, OrderConfirmationActivity::class.java)
                intent.putExtra("id_pedido", idPedido)
                startActivity(intent)
                finish()
            }.onFailure {
                Toast.makeText(this@CartActivity, "No se pudo confirmar el pedido: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

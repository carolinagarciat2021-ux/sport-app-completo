package com.sportapp.tiendasportmobile.ui.orders

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.model.DetallePedidoItem
import com.sportapp.tiendasportmobile.data.network.PedidoResponseItem
import com.sportapp.tiendasportmobile.ui.base.BaseDrawerActivity
import kotlinx.coroutines.launch

class PedidosActivity : BaseDrawerActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var tvVacios: TextView
    private lateinit var adapter: PedidoAdapter

    override fun layoutRecursoPropio(): Int = R.layout.activity_pedidos
    override fun tituloPantalla(): String = "Pedidos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        swipeRefresh = findViewById(R.id.swipeRefreshPedidos)
        recycler = findViewById(R.id.recyclerPedidos)
        tvVacios = findViewById(R.id.tvVaciosPedidos)

        val idRol = repository.session.obtenerIdRol()
        val esAdminOVendedor = idRol == 1 || idRol == 3

        adapter = PedidoAdapter(esAdminOVendedor) { idPedido ->
            // Esta lambda la llama el adapter la primera vez que se expande un pedido
            val resultado = repository.listarDetallePedido(idPedido)
            resultado.getOrDefault(emptyList())
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        swipeRefresh.setOnRefreshListener { cargarPedidos() }
        cargarPedidos()
    }

    private fun cargarPedidos() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val resultado = repository.listarPedidos()
            swipeRefresh.isRefreshing = false
            resultado.onSuccess { lista ->
                adapter.actualizarLista(lista)
                tvVacios.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                calcularTotalesEnSegundoPlano(lista)
            }.onFailure {
                android.widget.Toast.makeText(
                    this@PedidosActivity, "No se pudieron cargar los pedidos: ${it.message}", android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /** Pide el detalle de CADA pedido para poder mostrar el total ya calculado sin esperar a que lo expandan. */
    private fun calcularTotalesEnSegundoPlano(pedidos: List<PedidoResponseItem>) {
        pedidos.forEach { pedido ->
            lifecycleScope.launch {
                val detalle: List<DetallePedidoItem> = repository.listarDetallePedido(pedido.id_pedido).getOrDefault(emptyList())
                val total = detalle.sumOf { it.subtotal() }
                adapter.fijarTotal(pedido.id_pedido, total)
            }
        }
    }
}

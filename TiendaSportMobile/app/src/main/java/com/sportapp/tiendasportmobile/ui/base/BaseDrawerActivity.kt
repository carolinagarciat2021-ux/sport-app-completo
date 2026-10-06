package com.sportapp.tiendasportmobile.ui.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.sportapp.tiendasportmobile.R
import com.sportapp.tiendasportmobile.data.repository.TiendaRepository
import com.sportapp.tiendasportmobile.ui.cart.CartActivity
import com.sportapp.tiendasportmobile.ui.catalog.CatalogActivity
import com.sportapp.tiendasportmobile.ui.login.LoginActivity
import com.sportapp.tiendasportmobile.ui.orders.PedidosActivity

/**
 * Pantalla base con menú lateral (drawer). Cada Activity real (CatalogActivity,
 * CartActivity, PedidosActivity, las futuras de administrador, etc.) hereda de
 * esta clase en vez de AppCompatActivity.
 */
abstract class BaseDrawerActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var repository: TiendaRepository
    private val handlerAutoOcultar = Handler(Looper.getMainLooper())
    private val AUTO_OCULTAR_MS = 4000L

    @LayoutRes
    protected abstract fun layoutRecursoPropio(): Int

    protected abstract fun tituloPantalla(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_drawer)
        repository = TiendaRepository(this)

        val contenedor = findViewById<FrameLayout>(R.id.contenidoFrame)
        LayoutInflater.from(this).inflate(layoutRecursoPropio(), contenedor, true)

        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = tituloPantalla()
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            } else {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
            }
        }

        val navView = findViewById<NavigationView>(R.id.navView)
        configurarEncabezado(navView)
        configurarVisibilidadSegunRol(navView)
        configurarClicsDelMenu(navView)
        configurarAutoOcultado()
    }

    private fun configurarEncabezado(navView: NavigationView) {
        val header = navView.getHeaderView(0)
        val tvNombre = header.findViewById<TextView>(R.id.tvNavNombre)
        val tvRol = header.findViewById<TextView>(R.id.tvNavRol)

        if (repository.session.haySesionActiva()) {
            tvNombre.text = repository.session.obtenerNombre() ?: "Usuario"
            tvRol.text = nombreDelRol(repository.session.obtenerIdRol())
        } else {
            tvNombre.text = "Invitado"
            tvRol.text = "Sin sesión — solo catálogo"
        }
    }

    private fun nombreDelRol(idRol: Int): String = when (idRol) {
        1 -> "Administrador"
        3 -> "Vendedor"
        else -> "Cliente"
    }

    /**
     * Administrador (1): ve los 5 items de administración — crea/edita/elimina todo.
     * Vendedor (3): SOLO "Pedidos de Clientes" — vende, no toca inventario/categorías/usuarios.
     * Cliente (2) o invitado: ninguno de los 5.
     */
    private fun configurarVisibilidadSegunRol(navView: NavigationView) {
        val haySesion = repository.session.haySesionActiva()
        val idRol = repository.session.obtenerIdRol()
        val esAdmin = haySesion && idRol == 1
        val esVendedor = haySesion && idRol == 3

        navView.menu.findItem(R.id.nav_inventario)?.isVisible = esAdmin
        navView.menu.findItem(R.id.nav_registrar_producto)?.isVisible = esAdmin
        navView.menu.findItem(R.id.nav_categorias)?.isVisible = esAdmin
        navView.menu.findItem(R.id.nav_usuarios)?.isVisible = esAdmin
        navView.menu.findItem(R.id.nav_pedidos_admin)?.isVisible = esAdmin || esVendedor

        navView.menu.findItem(R.id.nav_mis_pedidos)?.isVisible = haySesion
        navView.menu.findItem(R.id.nav_cerrar_sesion)?.isVisible = haySesion
    }

    private fun configurarClicsDelMenu(navView: NavigationView) {
        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.nav_catalogo -> irA(CatalogActivity::class.java)
                R.id.nav_carrito -> irA(CartActivity::class.java)
                // "Mis Pedidos" (cliente/vendedor) y "Pedidos de Clientes" (admin/vendedor)
                // son LA MISMA pantalla: el backend ya decide qué pedidos devolver según el rol.
                R.id.nav_mis_pedidos -> irA(PedidosActivity::class.java)
                R.id.nav_pedidos_admin -> irA(PedidosActivity::class.java)

                R.id.nav_inventario -> irA(com.sportapp.tiendasportmobile.ui.inventario.InventarioActivity::class.java)
                R.id.nav_registrar_producto -> startActivity(Intent(this, com.sportapp.tiendasportmobile.ui.inventario.ProductoFormActivity::class.java))
                R.id.nav_categorias -> irA(com.sportapp.tiendasportmobile.ui.categorias.CategoriaActivity::class.java)
                R.id.nav_usuarios -> irA(com.sportapp.tiendasportmobile.ui.usuarios.UsuarioActivity::class.java)

                R.id.nav_cerrar_sesion -> {
                    repository.session.cerrarSesion()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    private fun irA(destino: Class<*>) {
        if (this::class.java != destino) {
            startActivity(Intent(this, destino))
        }
    }

    private fun configurarAutoOcultado() {
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: android.view.View) {
                handlerAutoOcultar.postDelayed({ drawerLayout.closeDrawers() }, AUTO_OCULTAR_MS)
            }
            override fun onDrawerClosed(drawerView: android.view.View) {
                handlerAutoOcultar.removeCallbacksAndMessages(null)
            }
        })
    }

    override fun onDestroy() {
        handlerAutoOcultar.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

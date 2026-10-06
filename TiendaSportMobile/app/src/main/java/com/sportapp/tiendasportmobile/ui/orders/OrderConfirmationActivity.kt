package com.sportapp.tiendasportmobile.ui.orders

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sportapp.tiendasportmobile.TiendaSportApp
import com.sportapp.tiendasportmobile.databinding.ActivityOrderConfirmationBinding
import com.sportapp.tiendasportmobile.ui.catalog.CatalogActivity

class OrderConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idPedido = intent.getIntExtra("id_pedido", 0)
        binding.tvMensaje.text = "Tu pedido #$idPedido quedó registrado correctamente.\n\nYa se guardó en la base de datos y el stock se descontó automáticamente."

        // Firebase Analytics: registra el evento de compra completada.
        // Es la tecnología emergente incorporada al módulo de pedidos.
        val eventBundle = Bundle()
        eventBundle.putInt("id_pedido", idPedido)
        (application as TiendaSportApp).firebaseAnalytics.logEvent("compra_confirmada", eventBundle)

        binding.btnSeguirComprando.setOnClickListener {
            val intent = Intent(this, CatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}

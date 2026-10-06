package com.sportapp.tiendasportmobile.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sportapp.tiendasportmobile.data.repository.TiendaRepository
import com.sportapp.tiendasportmobile.databinding.ActivityLoginBinding
import com.sportapp.tiendasportmobile.ui.catalog.CatalogActivity
import com.sportapp.tiendasportmobile.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: TiendaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = TiendaRepository(this)

        // Si ya había una sesión guardada (SharedPreferences), saltamos directo al catálogo,
        // igual que el frontend web restaura la sesión desde localStorage al refrescar.
        if (repository.session.haySesionActiva()) {
            irAlCatalogo()
            return
        }

        binding.spinnerRol.setSelection(0) // Cliente por defecto

        binding.btnIniciarSesion.setOnClickListener { intentarLogin() }
        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnEntrarInvitado.setOnClickListener { irAlCatalogo() }
    }

    private fun intentarLogin() {
        val correo = binding.etCorreo.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val rol = binding.spinnerRol.selectedItem.toString()

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnIniciarSesion.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE

        // Corrutina: petición de red asíncrona sin bloquear la interfaz (tecnología
        // vista en el material "Tareas Asíncronas" del curso).
        lifecycleScope.launch {
            val resultado = repository.iniciarSesion(correo, password, rol)
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnIniciarSesion.isEnabled = true

            resultado.onSuccess {
                Toast.makeText(this@LoginActivity, "¡Bienvenido, ${it.nombre}!", Toast.LENGTH_SHORT).show()
                irAlCatalogo()
            }.onFailure {
                Toast.makeText(this@LoginActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun irAlCatalogo() {
        startActivity(Intent(this, CatalogActivity::class.java))
        finish()
    }
}

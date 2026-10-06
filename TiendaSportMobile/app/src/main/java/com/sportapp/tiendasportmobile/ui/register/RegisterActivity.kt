package com.sportapp.tiendasportmobile.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sportapp.tiendasportmobile.data.model.Cliente
import com.sportapp.tiendasportmobile.data.repository.TiendaRepository
import com.sportapp.tiendasportmobile.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: TiendaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = TiendaRepository(this)

        binding.btnRegistrar.setOnClickListener { intentarRegistro() }
    }

    private fun intentarRegistro() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val identificacion = binding.etIdentificacion.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (listOf(nombre, apellido, identificacion, telefono, direccion, correo, password).any { it.isEmpty() }) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Misma regla de complejidad que valida el backend: se revisa aquí también
        // para avisarle al usuario al instante, sin esperar la respuesta del servidor.
        val tieneLetra = password.any { it.isLetter() }
        val tieneNumero = password.any { it.isDigit() }
        val tieneSimbolo = password.any { !it.isLetterOrDigit() }
        if (password.length < 8 || !tieneLetra || !tieneNumero || !tieneSimbolo) {
            Toast.makeText(this, "La contraseña debe tener mínimo 8 caracteres, con letra, número y símbolo", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnRegistrar.isEnabled = false
        lifecycleScope.launch {
            val resultado = repository.registrarCliente(
                Cliente(
                    nombre = nombre, apellido = apellido, identificacion = identificacion,
                    telefono = telefono, direccion = direccion, correo = correo,
                    password = password, id_rol = 2
                )
            )
            binding.btnRegistrar.isEnabled = true
            resultado.onSuccess {
                Toast.makeText(this@RegisterActivity, "¡Registro exitoso! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                finish()
            }.onFailure {
                Toast.makeText(this@RegisterActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

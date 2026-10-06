package com.sportapp.tiendasport.controller;

import com.sportapp.tiendasport.dto.MensajeResponse;
import com.sportapp.tiendasport.model.Cliente;
import com.sportapp.tiendasport.repository.ClienteRepository;
import com.sportapp.tiendasport.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    // Solo Administrador (ver SecurityConfig) — nunca devolvemos el campo password
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Map<String, Object>> listar() {
        return clienteRepository.findAll().stream().map(c -> {
            Map<String, Object> mapa = new java.util.HashMap<>();
            mapa.put("id_clientes", c.getIdClientes());
            mapa.put("nombre", c.getNombre());
            mapa.put("apellido", c.getApellido());
            mapa.put("identificacion", c.getIdentificacion());
            mapa.put("telefono", c.getTelefono());
            mapa.put("direccion", c.getDireccion());
            mapa.put("correo", c.getCorreo());
            mapa.put("id_rol", c.getIdRol());
            // Ojo: "password" NUNCA se incluye aquí, ni siquiera cifrada.
            return mapa;
        }).toList();
    }

    // El propio cliente puede editar su perfil; un Administrador puede editar a cualquiera
    // (esta regla fina se valida aquí adentro, además de exigir "authenticated" en SecurityConfig)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Map<String, Object> datos,
                                         @AuthenticationPrincipal Cliente solicitante) {
        boolean esAdmin = solicitante.getIdRol() == 1;
        if (!esAdmin && !solicitante.getIdClientes().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MensajeResponse.error("Solo puedes editar tu propio perfil"));
        }

        return clienteRepository.findById(id).map(cliente -> {
            if (datos.get("nombre") != null) cliente.setNombre((String) datos.get("nombre"));
            if (datos.get("apellido") != null) cliente.setApellido((String) datos.get("apellido"));
            if (datos.get("identificacion") != null) cliente.setIdentificacion((String) datos.get("identificacion"));
            if (datos.get("telefono") != null) cliente.setTelefono((String) datos.get("telefono"));
            if (datos.get("direccion") != null) cliente.setDireccion((String) datos.get("direccion"));
            if (datos.get("correo") != null) cliente.setCorreo((String) datos.get("correo"));

            // Contraseña nueva: solo si viene y no está vacía; si no, se conserva la actual
            Object nuevaPassword = datos.get("password");
            if (nuevaPassword != null && !((String) nuevaPassword).isBlank()) {
                String errorPassword = PasswordValidator.validar((String) nuevaPassword);
                if (errorPassword != null) {
                    return ResponseEntity.badRequest().body(MensajeResponse.error(errorPassword));
                }
                cliente.setPassword(passwordEncoder.encode((String) nuevaPassword));
            }

            // Solo un Administrador puede cambiar el rol de alguien
            if (esAdmin && datos.get("id_rol") != null) {
                cliente.setIdRol(((Number) datos.get("id_rol")).intValue());
            }

            clienteRepository.save(cliente);
            return ResponseEntity.ok(MensajeResponse.ok("Cliente actualizado correctamente"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No se encontró el cliente")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No existe ese cliente"));
        }
        clienteRepository.deleteById(id);
        return ResponseEntity.ok(MensajeResponse.ok("Cliente eliminado"));
    }
}

package com.sportapp.tiendasport.controller;

import com.sportapp.tiendasport.dto.AuthDtos.*;
import com.sportapp.tiendasport.dto.MensajeResponse;
import com.sportapp.tiendasport.model.Cliente;
import com.sportapp.tiendasport.repository.ClienteRepository;
import com.sportapp.tiendasport.security.JwtUtil;
import com.sportapp.tiendasport.util.PasswordValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private static final Map<String, Integer> ROLES = Map.of(
            "Administrador", 1,
            "Cliente", 2,
            "Vendedor", 3
    );

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Cliente cliente = clienteRepository.findByCorreo(request.getCorreo()).orElse(null);

        // Mismo cuidado que en el backend anterior: no revelamos si el correo existe
        // o no, para no ayudar a alguien a "adivinar" cuentas válidas.
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MensajeResponse.error("Correo o contraseña incorrectos"));
        }

        boolean coincide = verificarPasswordConMigracion(cliente, request.getPassword());
        if (!coincide) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MensajeResponse.error("Correo o contraseña incorrectos"));
        }

        Integer idRolEsperado = ROLES.get(request.getRol());
        if (idRolEsperado != null && !idRolEsperado.equals(cliente.getIdRol())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MensajeResponse.error("El rol seleccionado no coincide con este usuario"));
        }

        String token = jwtUtil.generarToken(cliente.getIdClientes(), cliente.getCorreo(), cliente.getIdRol());

        return ResponseEntity.ok(new LoginResponse(
                cliente.getIdClientes(), cliente.getNombre(), cliente.getApellido(),
                cliente.getCorreo(), cliente.getIdRol(), token
        ));
    }

    /**
     * Verifica la contraseña, y de paso resuelve un problema real de la migración:
     * las cuentas que ya existían en la base de datos (creadas con el backend
     * anterior, en Java plano) tienen la contraseña guardada en TEXTO PLANO,
     * no en BCrypt. No teníamos forma de recalcular esos hashes sin herramientas
     * externas, así que en vez de eso hacemos una "migración perezosa":
     *
     * 1. Primero probamos si coincide como hash de BCrypt (el caso normal,
     *    para cuentas creadas ya con este backend nuevo).
     * 2. Si no, probamos si coincide como texto plano (cuentas viejas).
     * 3. Si coincide en texto plano, quiere decir que la contraseña es correcta
     *    pero todavía no está protegida — la volvemos a guardar YA CIFRADA con
     *    BCrypt en ese mismo instante. Así, después de un solo login exitoso,
     *    esa cuenta queda migrada para siempre y nunca vuelve a compararse en
     *    texto plano.
     */
    private boolean verificarPasswordConMigracion(Cliente cliente, String passwordIngresada) {
        String passwordGuardada = cliente.getPassword();
        if (passwordGuardada == null) return false;

        boolean pareceHashBcrypt = passwordGuardada.startsWith("$2a$")
                || passwordGuardada.startsWith("$2b$")
                || passwordGuardada.startsWith("$2y$");

        if (pareceHashBcrypt) {
            return passwordEncoder.matches(passwordIngresada, passwordGuardada);
        }

        // Cuenta vieja con contraseña en texto plano
        boolean coincideTextoPlano = passwordGuardada.equals(passwordIngresada);
        if (coincideTextoPlano) {
            cliente.setPassword(passwordEncoder.encode(passwordIngresada));
            clienteRepository.save(cliente);
        }
        return coincideTextoPlano;
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest request) {
        String errorPassword = PasswordValidator.validar(request.getPassword());
        if (errorPassword != null) {
            return ResponseEntity.badRequest().body(MensajeResponse.error(errorPassword));
        }
        if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            return ResponseEntity.badRequest().body(MensajeResponse.error("Ya existe un cliente con esa identificación"));
        }
        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            return ResponseEntity.badRequest().body(MensajeResponse.error("Ya existe un cliente con ese correo"));
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setIdentificacion(request.getIdentificacion());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setCorreo(request.getCorreo());
        cliente.setPassword(passwordEncoder.encode(request.getPassword())); // ¡Aquí se cifra!
        cliente.setIdRol(2); // Cliente por defecto, igual que en la versión web

        clienteRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeResponse.ok("Cliente registrado exitosamente"));
    }
}

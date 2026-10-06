package com.sportapp.tiendasport.exception;

import com.sportapp.tiendasport.dto.MensajeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte cualquier excepción no controlada en una respuesta JSON con el
 * mismo formato que ya usa toda la API: {"error": "mensaje"}. Así el frontend
 * web y la app de Android no tienen que manejar formatos de error distintos
 * según de dónde venga la falla.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(MensajeResponse.error(mensaje));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MensajeResponse> manejarAccesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(MensajeResponse.error("No tienes permisos suficientes para realizar esta acción"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MensajeResponse> manejarCredenciales(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MensajeResponse.error("Credenciales inválidas"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeResponse> manejarGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MensajeResponse.error("Error interno: " + ex.getMessage()));
    }
}

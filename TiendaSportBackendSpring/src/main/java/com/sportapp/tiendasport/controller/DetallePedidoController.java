package com.sportapp.tiendasport.controller;

import com.sportapp.tiendasport.dto.MensajeResponse;
import com.sportapp.tiendasport.dto.PedidoDtos.DetalleRequest;
import com.sportapp.tiendasport.model.Cliente;
import com.sportapp.tiendasport.model.DetallePedido;
import com.sportapp.tiendasport.model.Pedido;
import com.sportapp.tiendasport.repository.DetallePedidoRepository;
import com.sportapp.tiendasport.repository.PedidoRepository;
import com.sportapp.tiendasport.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detalle_pedido")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final PedidoService pedidoService;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;

    @PostMapping
    public ResponseEntity<?> agregar(@Valid @RequestBody DetalleRequest request, @AuthenticationPrincipal Cliente cliente) {
        try {
            DetallePedido detalle = pedidoService.agregarDetalle(request);
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Detalle de pedido registrado y stock actualizado");
            respuesta.put("id_detalle", detalle.getIdDetalle());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MensajeResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Integer id_pedido,
                                     @AuthenticationPrincipal Cliente cliente) {
        if (id_pedido != null) {
            // Un Cliente solo puede consultar el detalle de SUS PROPIOS pedidos
            if (cliente.getIdRol() == 2) {
                Pedido pedido = pedidoRepository.findById(id_pedido).orElse(null);
                if (pedido == null || !pedido.getIdCliente().equals(cliente.getIdClientes())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(MensajeResponse.error("No tienes permiso para ver este pedido"));
                }
            }
            return ResponseEntity.ok(detallePedidoRepository.findByIdPedido(id_pedido));
        }

        if (cliente.getIdRol() == 1 || cliente.getIdRol() == 3) {
            return ResponseEntity.ok(detallePedidoRepository.findAll());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MensajeResponse.error("Debes indicar 'id_pedido'"));
    }
}

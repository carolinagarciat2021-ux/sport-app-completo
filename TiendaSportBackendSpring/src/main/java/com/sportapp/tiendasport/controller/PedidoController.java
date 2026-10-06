package com.sportapp.tiendasport.controller;

import com.sportapp.tiendasport.dto.MensajeResponse;
import com.sportapp.tiendasport.dto.PedidoDtos.PedidoRequest;
import com.sportapp.tiendasport.dto.PedidoDtos.PedidoResponse;
import com.sportapp.tiendasport.model.Cliente;
import com.sportapp.tiendasport.model.Pedido;
import com.sportapp.tiendasport.repository.PedidoRepository;
import com.sportapp.tiendasport.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    @GetMapping
    public List<Pedido> listar(@AuthenticationPrincipal Cliente cliente) {
        boolean esAdminOVendedor = cliente.getIdRol() == 1 || cliente.getIdRol() == 3;
        // Cliente: solo ve sus propios pedidos. Administrador/Vendedor: ven todos.
        // (Spring Security ya garantiza que "cliente" aquí es siempre alguien autenticado.)
        return esAdminOVendedor ? pedidoRepository.findAll() : pedidoRepository.findByIdCliente(cliente.getIdClientes());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody PedidoRequest request, @AuthenticationPrincipal Cliente cliente) {
        // El id_cliente SIEMPRE sale del token autenticado, nunca de lo que mande el body,
        // para que nadie pueda crear un pedido a nombre de otra persona.
        Pedido pedido = pedidoService.crearPedido(cliente.getIdClientes(), request.getEstado());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PedidoResponse("Pedido registrado exitosamente", pedido.getIdPedido()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id, @RequestBody PedidoRequest request) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEstado(request.getEstado());
            pedidoRepository.save(pedido);
            return ResponseEntity.ok(MensajeResponse.ok("Pedido actualizado correctamente"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No se encontró el pedido")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No existe ese pedido"));
        }
        pedidoRepository.deleteById(id);
        return ResponseEntity.ok(MensajeResponse.ok("Pedido eliminado"));
    }
}

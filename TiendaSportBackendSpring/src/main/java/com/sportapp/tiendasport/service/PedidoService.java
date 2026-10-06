package com.sportapp.tiendasport.service;

import com.sportapp.tiendasport.dto.PedidoDtos.DetalleRequest;
import com.sportapp.tiendasport.model.DetallePedido;
import com.sportapp.tiendasport.model.Pedido;
import com.sportapp.tiendasport.model.Producto;
import com.sportapp.tiendasport.repository.DetallePedidoRepository;
import com.sportapp.tiendasport.repository.PedidoRepository;
import com.sportapp.tiendasport.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;

    public Pedido crearPedido(Integer idCliente, String estado) {
        Pedido pedido = new Pedido();
        pedido.setIdCliente(idCliente);
        pedido.setEstado(estado != null ? estado : "Pendiente");
        return pedidoRepository.save(pedido);
    }

    /**
     * @Transactional: si algo falla a mitad de camino (por ejemplo, no hay stock
     * suficiente), TODO se revierte — no queda ni el detalle ni el descuento a medias.
     * El findWithLockByIdProducto bloquea la fila mientras dura la operación, para
     * que dos compras simultáneas del mismo producto no descuenten el stock dos veces.
     */
    @Transactional
    public DetallePedido agregarDetalle(DetalleRequest request) {
        Producto producto = productoRepository.findWithLockByIdProducto(request.getId_producto())
                .orElseThrow(() -> new IllegalArgumentException("El producto ID " + request.getId_producto() + " no existe."));

        if (producto.getStock() < request.getCantidad()) {
            throw new IllegalStateException(
                "Stock insuficiente para el producto ID " + request.getId_producto() +
                " (disponible: " + producto.getStock() + ", solicitado: " + request.getCantidad() + ")"
            );
        }

        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepository.save(producto);

        DetallePedido detalle = new DetallePedido();
        detalle.setIdPedido(request.getId_pedido());
        detalle.setIdProducto(request.getId_producto());
        detalle.setTalla(request.getTalla());
        detalle.setColor(request.getColor());
        detalle.setCantidad(request.getCantidad());
        detalle.setPrecioUnitario(request.getPrecio_unitario());

        return detallePedidoRepository.save(detalle);
    }
}

package com.sportapp.tiendasport.repository;

import com.sportapp.tiendasport.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
    List<DetallePedido> findByIdPedido(Integer idPedido);
}

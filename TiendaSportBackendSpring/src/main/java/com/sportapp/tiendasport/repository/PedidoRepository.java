package com.sportapp.tiendasport.repository;

import com.sportapp.tiendasport.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByIdCliente(Integer idCliente);
}

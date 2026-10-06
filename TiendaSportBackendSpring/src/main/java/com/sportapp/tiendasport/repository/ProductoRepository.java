package com.sportapp.tiendasport.repository;

import com.sportapp.tiendasport.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Bloquea la fila del producto mientras dura la transacción de compra,
    // para que dos compras al mismo tiempo no descuenten el mismo stock dos veces
    // (el mismo cuidado que ya teníamos en el backend anterior, ahora con JPA).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Producto> findWithLockByIdProducto(Integer idProducto);
}

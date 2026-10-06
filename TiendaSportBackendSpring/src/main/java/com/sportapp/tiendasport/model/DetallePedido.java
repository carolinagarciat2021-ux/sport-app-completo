package com.sportapp.tiendasport.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(name = "id_pedido", nullable = false)
    private Integer idPedido;

    @Column(name = "id_variante")
    private Integer idVariante;

    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    @Column(length = 30)
    private String talla;

    @Column(length = 50)
    private String color;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private java.math.BigDecimal precioUnitario;
}

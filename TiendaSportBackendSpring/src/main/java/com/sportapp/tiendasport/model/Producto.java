package com.sportapp.tiendasport.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 100)
    private String descripcion;

    @Column(nullable = false, length = 30)
    private String talla;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(nullable = false, length = 20)
    private String genero = "Unisex";

    @Column(name = "precio_mayorista", nullable = false)
    private java.math.BigDecimal precioMayorista;

    @Column(name = "costo_producto", nullable = false)
    private java.math.BigDecimal costoProducto;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "imagenes_por_color", columnDefinition = "TEXT")
    private String imagenesPorColor;

    @Column(name = "id_categoria")
    private Integer idCategoria;
}

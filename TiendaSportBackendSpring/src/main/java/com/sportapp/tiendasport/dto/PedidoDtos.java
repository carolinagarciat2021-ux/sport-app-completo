package com.sportapp.tiendasport.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class PedidoDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PedidoRequest {
        private String estado = "Pendiente";
    }

    @Data
    @AllArgsConstructor
    public static class PedidoResponse {
        private String mensaje;
        private Integer id_pedido;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleRequest {
        @NotNull
        private Integer id_pedido;
        @NotNull
        private Integer id_producto;
        private String talla;
        private String color;
        @NotNull @Min(1)
        private Integer cantidad;
        @NotNull
        private BigDecimal precio_unitario;
    }
}

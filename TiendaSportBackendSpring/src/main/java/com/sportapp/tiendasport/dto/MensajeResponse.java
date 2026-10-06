package com.sportapp.tiendasport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MensajeResponse {
    private String mensaje;
    private String error;

    public static MensajeResponse ok(String mensaje) { return new MensajeResponse(mensaje, null); }
    public static MensajeResponse error(String error) { return new MensajeResponse(null, error); }
}

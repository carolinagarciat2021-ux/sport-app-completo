package com.sportapp.tiendasport.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank @Email
        private String correo;
        @NotBlank
        private String password;
        @NotBlank
        private String rol; // "Cliente", "Administrador" o "Vendedor" — se valida contra el id_rol real
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private Integer id_clientes;
        private String nombre;
        private String apellido;
        private String correo;
        private Integer id_rol;
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistroRequest {
        @NotBlank
        private String nombre;
        @NotBlank
        private String apellido;
        @NotBlank
        private String identificacion;
        @NotBlank
        private String telefono;
        @NotBlank
        private String direccion;
        @NotBlank @Email
        private String correo;
        @NotBlank
        private String password;
    }
}

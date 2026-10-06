package com.sportapp.tiendasport.util;

public class PasswordValidator {

    /** Devuelve null si la contraseña es válida, o el mensaje de error si no. */
    public static String validar(String password) {
        if (password == null || password.length() < 8) {
            return "La contraseña debe tener mínimo 8 caracteres.";
        }
        boolean tieneLetra = password.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = password.chars().anyMatch(Character::isDigit);
        boolean tieneSimbolo = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        if (!tieneLetra || !tieneNumero || !tieneSimbolo) {
            return "La contraseña debe incluir al menos una letra, un número y un símbolo.";
        }
        return null;
    }
}

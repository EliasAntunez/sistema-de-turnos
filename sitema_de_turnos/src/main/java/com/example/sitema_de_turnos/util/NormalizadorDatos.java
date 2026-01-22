package com.example.sitema_de_turnos.util;

public class NormalizadorDatos {
    /**
     * Normaliza un nombre o apellido: "juan peRez" -> "Juan Perez"
     */
    public static String normalizarNombre(String input) {
        if (input == null) return null;
        String trimmed = input.trim().replaceAll(" +", " ");
        // Permitir letras, espacios, tildes y apóstrofes
        if (!trimmed.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ' ]+")) {
            throw new IllegalArgumentException("El nombre/apellido solo puede contener letras y espacios");
        }
        String[] palabras = trimmed.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.isEmpty()) continue;
            sb.append(Character.toUpperCase(palabra.charAt(0)));
            if (palabra.length() > 1) {
                sb.append(palabra.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Normaliza un email: trim y minúsculas
     */
    public static String normalizarEmail(String input) {
        if (input == null) return null;
        return input.trim().toLowerCase();
    }

    /**
     * Normaliza un teléfono: solo dígitos
     */
    public static String normalizarTelefono(String input) {
        if (input == null) return null;
        return input.replaceAll("\\D", "");
    }
}

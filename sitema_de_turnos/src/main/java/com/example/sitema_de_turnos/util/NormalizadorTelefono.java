package com.example.sitema_de_turnos.util;

import com.example.sitema_de_turnos.excepcion.ValidacionException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para normalizar números de teléfono al formato E.164.
 * 
 * El formato E.164 es el estándar internacional para números de teléfono:
 * - Empieza con + (opcional en input, obligatorio en output)
 * - Código de país (ej: 54 para Argentina)
 * - Código de área sin 0 inicial
 * - Número local
 * 
 * Ejemplos:
 * - Input: "3764123456" → Output: "+543764123456"
 * - Input: "03764123456" → Output: "+543764123456"
 * - Input: "+54 3764 123456" → Output: "+543764123456"
 * - Input: "(011) 4567-8900" → Output: "+541145678900"
 */
@Slf4j
public class NormalizadorTelefono {

    private static final String CODIGO_PAIS_ARGENTINA = "54";
    
    /**
     * Normaliza un teléfono al formato E.164.
     * Por defecto asume código de país Argentina (+54).
     * 
     * @param telefono Teléfono a normalizar (puede tener espacios, guiones, paréntesis)
     * @return Teléfono en formato E.164 (ej: +543764123456)
     * @throws ValidacionException Si el teléfono no es válido
     */
    public static String normalizarE164(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new ValidacionException("Teléfono no puede estar vacío");
        }
        
        // 1. Limpiar: eliminar espacios, guiones, paréntesis, etc.
        String limpio = telefono.replaceAll("[\\s\\-\\(\\)\\.]", "");
        
        // 2. Si ya tiene +, validar y retornar
        if (limpio.startsWith("+")) {
            validarFormatoE164(limpio);
            return limpio;
        }
        
        // 3. Si empieza con 54 (código de país sin +), agregar +
        if (limpio.startsWith("54") && limpio.length() >= 12) {
            String resultado = "+" + limpio;
            validarFormatoE164(resultado);
            return resultado;
        }
        
        // 4. Si empieza con 0 (código de área con 0), eliminarlo
        if (limpio.startsWith("0")) {
            limpio = limpio.substring(1);
        }
        
        // 5. Agregar código de país
        String resultado = "+" + CODIGO_PAIS_ARGENTINA + limpio;
        
        // 6. Validar formato final
        validarFormatoE164(resultado);
        
        log.debug("Teléfono normalizado: {} → {}", telefono, resultado);
        
        return resultado;
    }
    
    /**
     * Valida que un teléfono cumpla con el formato E.164.
     * 
     * @param telefono Teléfono a validar
     * @throws ValidacionException Si no cumple el formato
     */
    private static void validarFormatoE164(String telefono) {
        // Formato E.164: + seguido de 7 a 15 dígitos
        if (!telefono.matches("^\\+[1-9]\\d{6,14}$")) {
            throw new ValidacionException(
                "Teléfono no válido: " + telefono + 
                ". Debe tener formato E.164 (ej: +543764123456)"
            );
        }
    }
    
    /**
     * Verifica si un teléfono ya está en formato E.164.
     * 
     * @param telefono Teléfono a verificar
     * @return true si está en formato E.164, false en caso contrario
     */
    public static boolean esFormatoE164(String telefono) {
        if (telefono == null) {
            return false;
        }
        return telefono.matches("^\\+[1-9]\\d{6,14}$");
    }
}

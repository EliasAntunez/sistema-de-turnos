package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Información pública sobre un teléfono dentro de una empresa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelefonoInfoResponse {
    private boolean existe; // existe un cliente con ese teléfono en la empresa
    private boolean tieneUsuario; // si el cliente tiene cuenta (tieneUsuario == true)
    private String nombreEnmascarado; // opcional: nombre parcialmente enmascarado para UX
}

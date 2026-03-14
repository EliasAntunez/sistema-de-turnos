package com.example.sitema_de_turnos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroEmpresaConDuenoRequest {

    @NotNull(message = "Los datos del dueño son obligatorios")
    @Valid
    private RegistroDuenoRequest dueno;

    @NotNull(message = "Los datos de la empresa son obligatorios")
    @Valid
    private RegistroEmpresaRequest empresa;

    /**
     * Si es {@code true}, el dueño también recibirá el rol PROFESIONAL
     * y se le creará un PerfilProfesional en la misma empresa.
     * Permite que el dueño gestione su propia agenda de turnos.
     */
    private boolean crearPerfilProfesional = false;
}

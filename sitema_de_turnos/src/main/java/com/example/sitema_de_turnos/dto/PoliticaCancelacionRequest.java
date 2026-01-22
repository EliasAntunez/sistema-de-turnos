package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.*;


public class PoliticaCancelacionRequest {
    // empresaId eliminado: el backend lo infiere del usuario autenticado

    @NotNull(message = "El tipo de política es obligatorio")
    private String tipo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @Min(value = 1, message = "Las horas límite deben ser mayores a 0")
    private int horasLimiteCancelacion;

    @NotNull(message = "La penalización es obligatoria")
    private String penalizacion;


    private String mensajeCliente;
    private boolean activa = true;


    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getHorasLimiteCancelacion() { return horasLimiteCancelacion; }
    public void setHorasLimiteCancelacion(int horasLimiteCancelacion) { this.horasLimiteCancelacion = horasLimiteCancelacion; }

    public String getPenalizacion() { return penalizacion; }
    public void setPenalizacion(String penalizacion) { this.penalizacion = penalizacion; }

    public String getMensajeCliente() { return mensajeCliente; }
    public void setMensajeCliente(String mensajeCliente) { this.mensajeCliente = mensajeCliente; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}

package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaApi<T> {
    
    private Boolean exito;
    private String mensaje;
    private T datos;
    private LocalDateTime timestamp;

    public RespuestaApi(Boolean exito, String mensaje, T datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> RespuestaApi<T> exitosa(String mensaje, T datos) {
        return new RespuestaApi<>(true, mensaje, datos);
    }

    public static <T> RespuestaApi<T> exitosa(T datos) {
        return new RespuestaApi<>(true, "Operación exitosa", datos);
    }

    public static <T> RespuestaApi<T> error(String mensaje) {
        return new RespuestaApi<>(false, mensaje, null);
    }
}

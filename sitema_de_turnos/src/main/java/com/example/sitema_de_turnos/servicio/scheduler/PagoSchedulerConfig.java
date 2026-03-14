package com.example.sitema_de_turnos.servicio.scheduler;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PagoSchedulerConfig {

    @Value("${app.pagos.expiracion-horas:24}")
    private int expiracionHoras;
}

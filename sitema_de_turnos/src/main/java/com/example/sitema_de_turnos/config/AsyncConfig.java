package com.example.sitema_de_turnos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Habilita el soporte de @Async en el contexto de Spring.
 *
 * El executor "notificacionesExecutor" es dedicado al envío de emails:
 * - corePoolSize 5: 5 hilos permanentes para carga base de notificaciones
 * - maxPoolSize 10: hasta 10 hilos bajo picos de envío
 * - queueCapacity 25: cola acotada para evitar crecimiento descontrolado
 * - CallerRunsPolicy: si la cola se llena, el hilo del llamador ejecuta la tarea
 *   (degradación controlada — nunca pierde un email silenciosamente)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificacionesExecutor")
    public Executor notificacionesExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("notif-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

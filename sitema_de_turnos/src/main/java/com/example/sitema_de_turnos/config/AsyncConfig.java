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
 * - corePoolSize 2: 2 hilos permanentes para picos habituales
 * - maxPoolSize 5: hasta 5 hilos bajo carga alta
 * - queueCapacity 100: cola para absorber ráfagas sin rechazar tareas
 * - CallerRunsPolicy: si la cola se llena, el hilo del llamador ejecuta la tarea
 *   (degradación controlada — nunca pierde un email silenciosamente)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificacionesExecutor")
    public Executor notificacionesExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notif-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

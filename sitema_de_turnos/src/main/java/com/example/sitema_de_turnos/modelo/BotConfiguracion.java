package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bot_configuraciones",
    indexes = {
        @Index(name = "idx_bot_config_instancia_whatsapp", columnList = "instancia_whatsapp", unique = true),
        @Index(name = "idx_bot_config_tenant", columnList = "tenant_id")
    }
)
public class BotConfiguracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instancia_whatsapp", nullable = false, unique = true, length = 120)
    private String instanciaWhatsapp;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "prompt_personalizado", columnDefinition = "TEXT")
    private String promptPersonalizado;

    @Column(name = "estado_bot", nullable = false)
    private Boolean estadoBot = true;
}

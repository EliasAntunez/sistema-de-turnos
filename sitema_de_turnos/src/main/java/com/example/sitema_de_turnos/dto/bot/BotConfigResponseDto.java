package com.example.sitema_de_turnos.dto.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotConfigResponseDto {

    private Long tenantId;
    private String promptPersonalizado;
    private Boolean estadoBotActivo;
}

package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.EspecialidadResponse;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioEspecialidad;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador público para consultar especialidades activas
 * Accesible por todos los roles autenticados (Dueño, Profesional, etc.)
 */
@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class ControladorEspecialidades {

    private final ServicioEspecialidad servicioEspecialidad;

    /**
     * Obtener solo las especialidades activas
     * Para usar en formularios de profesionales y servicios
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<EspecialidadResponse>>> obtenerEspecialidadesActivas() {
        
        List<EspecialidadResponse> especialidades = servicioEspecialidad.listarEspecialidadesActivas();
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Especialidades activas obtenidas exitosamente", especialidades)
        );
    }
}

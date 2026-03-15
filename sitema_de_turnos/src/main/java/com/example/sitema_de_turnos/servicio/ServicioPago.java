package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.EstadoPago;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.MetodoPago;
import com.example.sitema_de_turnos.modelo.Pago;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioPago;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.servicio.notificacion.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioPago {

    private final RepositorioTurno repositorioTurno;
    private final RepositorioPago repositorioPago;
    private final RepositorioPerfilDueno repositorioPerfilDueno;
    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public void confirmarPagoManual(String emailStaff, Long turnoId, MetodoPago metodoPago) {
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        Pago pago = repositorioPago.findByTurnoId(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado para el turno"));

        validarPermisosStaff(emailStaff, turno);

        if (metodoPago == MetodoPago.MERCADO_PAGO) {
            throw new ValidacionException("Para confirmación manual solo se permite EFECTIVO o TRANSFERENCIA");
        }

        if (turno.getEstado() != EstadoTurno.PENDIENTE_PAGO) {
            throw new ValidacionException("Solo se puede confirmar manualmente un turno en estado PENDIENTE_PAGO");
        }

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new ValidacionException("Solo se puede confirmar un pago en estado PENDIENTE");
        }

        pago.setMetodoPago(metodoPago);
        pago.setEstado(EstadoPago.APROBADO);
        turno.setEstado(EstadoTurno.CONFIRMADO);

        repositorioPago.save(pago);
        turno = repositorioTurno.save(turno);

        emailNotificationService.enviarCorreoConfirmacionTurno(turno);
    }

    private void validarPermisosStaff(String emailStaff, Turno turno) {
        boolean esDuenoDeLaEmpresa = repositorioPerfilDueno.findByUsuarioEmail(emailStaff)
            .map(dueno -> dueno.getEmpresa().getId().equals(turno.getEmpresa().getId()))
            .orElse(false);

        boolean esProfesionalDelTurno = repositorioPerfilProfesional.findByUsuarioEmail(emailStaff)
            .map(profesional -> profesional.getId().equals(turno.getProfesional().getId()))
            .orElse(false);

        if (!esDuenoDeLaEmpresa && !esProfesionalDelTurno) {
            throw new AccesoDenegadoException("No tienes permisos para confirmar el pago de este turno");
        }
    }
}

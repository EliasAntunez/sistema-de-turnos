package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.ProfesionalResponse;
import com.example.sitema_de_turnos.dto.ProfesionalServicioResponse;
import com.example.sitema_de_turnos.dto.RegistroProfesionalRequest;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.ConflictoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.mapper.ProfesionalMapper;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.PerfilDueno;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesionalServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioProfesional {

    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioProfesionalServicio repositorioProfesionalServicio;
    private final RepositorioServicio repositorioServicio;
    private final RepositorioTurno repositorioTurno;
    private final PasswordEncoder passwordEncoder;
    private final ServicioValidacionDueno servicioValidacionDueno;

    @Transactional(readOnly = true)
    public List<ProfesionalResponse> obtenerProfesionalesPorEmpresa(String emailDueno) {
        PerfilDueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        List<PerfilProfesional> profesionales = repositorioPerfilProfesional.findByEmpresa(dueno.getEmpresa());
        return profesionales.stream()
                .map(ProfesionalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfesionalResponse crearProfesional(String emailDueno, RegistroProfesionalRequest dto) {
        PerfilDueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        String emailNormalizado = com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(dto.getEmail());

        if (repositorioUsuario.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException("El email " + dto.getEmail() + " ya está registrado");
        }

        if (dto.getContrasena() == null || dto.getContrasena().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un profesional");
        }
        if (dto.getContrasena().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        // 1. Crear Usuario con rol PROFESIONAL
        Usuario usuario = new Usuario();
        usuario.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getNombre()));
        usuario.setApellido(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getApellido()));
        usuario.setEmail(emailNormalizado);
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setTelefono(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarTelefono(dto.getTelefono()));
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuario.getRoles().add(RolUsuario.PROFESIONAL);
        usuario = repositorioUsuario.save(usuario);

        // 2. Crear PerfilProfesional vinculado al Usuario y la Empresa del dueño
        PerfilProfesional perfil = new PerfilProfesional();
        perfil.setUsuario(usuario);
        perfil.setEmpresa(dueno.getEmpresa());
        perfil.setDescripcion(dto.getDescripcion());
        perfil.setActivo(true);

        PerfilProfesional guardado = repositorioPerfilProfesional.save(perfil);
        return ProfesionalMapper.toResponse(guardado);
    }

    @Transactional
    public ProfesionalResponse actualizarProfesional(String emailDueno, Long idProfesional, RegistroProfesionalRequest dto) {
        PerfilDueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        PerfilProfesional perfil = repositorioPerfilProfesional.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        if (!perfil.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este profesional");
        }

        Usuario usuario = perfil.getUsuario();

        // Validar email único si cambió
        if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail())) {
            String emailNormalizado = com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(dto.getEmail());
            if (repositorioUsuario.existsByEmail(emailNormalizado)) {
                throw new IllegalArgumentException("El email " + dto.getEmail() + " ya está registrado");
            }
            usuario.setEmail(emailNormalizado);
        }

        usuario.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getNombre()));
        usuario.setApellido(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getApellido()));
        usuario.setTelefono(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarTelefono(dto.getTelefono()));
        usuario.setFechaActualizacion(LocalDateTime.now());

        if (dto.getContrasena() != null && !dto.getContrasena().isEmpty()) {
            if (dto.getContrasena().length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            }
            usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        }

        repositorioUsuario.save(usuario);

        perfil.setDescripcion(dto.getDescripcion());
        PerfilProfesional actualizado = repositorioPerfilProfesional.save(perfil);

        return ProfesionalMapper.toResponse(actualizado);
    }

    @Transactional
    public void eliminarProfesional(String emailDueno, Long idProfesional) {
        desactivarProfesional(emailDueno, idProfesional);
    }

    @Transactional
    public void activarProfesional(String emailDueno, Long idProfesional) {
        cambiarEstadoProfesional(emailDueno, idProfesional, true);
    }

    @Transactional
    public void desactivarProfesional(String emailDueno, Long idProfesional) {
        cambiarEstadoProfesional(emailDueno, idProfesional, false);
    }

    private void cambiarEstadoProfesional(String emailDueno, Long idProfesional, boolean activo) {
        PerfilDueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        PerfilProfesional perfil = repositorioPerfilProfesional.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        if (!perfil.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este profesional");
        }

        if (!activo) {
            validarSinTurnosActivos(perfil);
        }

        perfil.setActivo(activo);
        repositorioPerfilProfesional.save(perfil);
    }

    private void validarSinTurnosActivos(PerfilProfesional perfil) {
        List<EstadoTurno> estadosActivos = Arrays.asList(
            EstadoTurno.PENDIENTE_CONFIRMACION,
            EstadoTurno.PENDIENTE_PAGO,
            EstadoTurno.CONFIRMADO
        );

        long cantidadTurnosActivos = repositorioTurno.countByProfesionalAndEstadoIn(perfil, estadosActivos);

        if (cantidadTurnosActivos > 0) {
            throw new ConflictoException(
                String.format("No se puede desactivar el profesional %s %s porque tiene %d turno(s) activo(s) " +
                             "(en estado PENDIENTE_CONFIRMACION, PENDIENTE_PAGO o CONFIRMADO). " +
                             "Debe cancelar o completar estos turnos antes de desactivar al profesional.",
                             perfil.getUsuario().getNombre(), perfil.getUsuario().getApellido(), cantidadTurnosActivos)
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ProfesionalServicioResponse> obtenerServiciosDeProfesional(String emailDueno, Long profesionalId) {
        PerfilDueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        PerfilProfesional perfil = repositorioPerfilProfesional.findById(profesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        if (!perfil.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para acceder a este profesional");
        }

        List<Servicio> servicios = repositorioServicio.findByEmpresaAndActivoTrue(dueno.getEmpresa());

        List<ProfesionalServicio> habilitaciones = repositorioProfesionalServicio.findByProfesionalAndActivoTrue(perfil);
        Set<Long> serviciosHabilitados = habilitaciones.stream()
                .map(ps -> ps.getServicio().getId())
                .collect(Collectors.toSet());

        List<ProfesionalServicioResponse> resultado = new ArrayList<>();

        for (Servicio servicio : servicios) {
            ProfesionalServicioResponse response = new ProfesionalServicioResponse();
            response.setServicioId(servicio.getId());
            response.setNombre(servicio.getNombre());
            response.setDescripcion(servicio.getDescripcion());
            response.setDuracionMinutos(servicio.getDuracionMinutos());
            response.setPrecio(servicio.getPrecio().doubleValue());
            response.setDisponible(serviciosHabilitados.contains(servicio.getId()));
            resultado.add(response);
        }

        return resultado;
    }

    @Transactional
    public void toggleServicioProfesional(String emailDueno, Long profesionalId, Long servicioId, Boolean disponible) {
        PerfilDueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        PerfilProfesional perfil = repositorioPerfilProfesional.findById(profesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        if (!perfil.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este profesional");
        }

        Servicio servicio = repositorioServicio.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));

        if (!servicio.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para acceder a este servicio");
        }

        if (!servicio.getActivo()) {
            throw new IllegalArgumentException(
                "No se puede modificar la disponibilidad de un servicio desactivado. " +
                "Active el servicio primero desde la gestión de servicios."
            );
        }

        Optional<ProfesionalServicio> existente = repositorioProfesionalServicio
                .findByProfesionalAndServicio(perfil, servicio);

        if (disponible) {
            if (existente.isEmpty()) {
                ProfesionalServicio ps = new ProfesionalServicio();
                ps.setProfesional(perfil);
                ps.setServicio(servicio);
                ps.setActivo(true);
                repositorioProfesionalServicio.save(ps);
            } else {
                ProfesionalServicio ps = existente.get();
                ps.setActivo(true);
                repositorioProfesionalServicio.save(ps);
            }
        } else {
            existente.ifPresent(repositorioProfesionalServicio::delete);
        }
    }
}
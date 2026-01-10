package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.DiaSemana;
import com.example.sitema_de_turnos.modelo.DisponibilidadProfesional;
import com.example.sitema_de_turnos.modelo.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioDisponibilidadProfesional extends JpaRepository<DisponibilidadProfesional, Long> {
    
    List<DisponibilidadProfesional> findByProfesional(Profesional profesional);
    
    List<DisponibilidadProfesional> findByProfesionalAndActivoTrue(Profesional profesional);
    
    List<DisponibilidadProfesional> findByProfesionalAndDiaSemana(Profesional profesional, DiaSemana diaSemana);
    
    List<DisponibilidadProfesional> findByProfesionalAndDiaSemanaAndActivoTrue(Profesional profesional, DiaSemana diaSemana);
}

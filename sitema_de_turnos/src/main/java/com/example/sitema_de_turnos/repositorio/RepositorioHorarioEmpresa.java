package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.DiaSemana;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.HorarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioHorarioEmpresa extends JpaRepository<HorarioEmpresa, Long> {

    /**
     * Obtiene todos los horarios activos de una empresa
     */
    List<HorarioEmpresa> findByEmpresaAndActivoTrue(Empresa empresa);

    /**
     * Obtiene todos los horarios de una empresa (activos e inactivos)
     */
    List<HorarioEmpresa> findByEmpresa(Empresa empresa);

    /**
     * Obtiene todos los horarios activos de una empresa para un día específico
     */
    List<HorarioEmpresa> findByEmpresaAndDiaSemanaAndActivoTrue(Empresa empresa, DiaSemana diaSemana);

    /**
     * Obtiene todos los horarios de una empresa para un día específico (activos e inactivos)
     */
    List<HorarioEmpresa> findByEmpresaAndDiaSemana(Empresa empresa, DiaSemana diaSemana);
}

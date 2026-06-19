package com.rrhh.Modulos.CU3_Nomina.Domain.repository;

import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface INominaRepository {

    Nomina guardar(Nomina nomina);

    Optional<Nomina> buscarPorId(Integer idNomina);

    List<Nomina> buscarPorEmpleadoYPeriodo(Long idEmpleado, String periodo);

    List<Nomina> buscarPorPeriodo(String periodo);

    List<Nomina> buscarPorEmpleadoYRango(Long idEmpleado, LocalDate fechaInicio, LocalDate fechaFin);

    boolean existePorEmpleadoYPeriodo(Long idEmpleado, String periodo);

    List<Nomina> buscarPorNombreEmpleado(String nombre);
}
package com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.repository;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.RegistroAsistencia;

import java.time.LocalDate;
import java.util.Optional;

public interface IRegistroAsistenciaRepository {

    RegistroAsistencia guardar(RegistroAsistencia registroAsistencia);

    Optional<RegistroAsistencia> buscarPorEmpleadoYFecha(Integer idEmpleado, LocalDate fecha);

    Optional<RegistroAsistencia> buscarPorId(Integer idRegistro);
}
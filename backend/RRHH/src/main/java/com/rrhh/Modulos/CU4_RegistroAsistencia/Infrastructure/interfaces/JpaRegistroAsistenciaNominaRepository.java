package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaRegistroAsistenciaNominaRepository extends JpaRepository<RegistroAsistenciaModel, Integer> {

    Optional<RegistroAsistenciaModel> findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(Integer idEmpleado, LocalDate fecha, String tipoRegistro);

    List<RegistroAsistenciaModel> findByEmpleado_IdEmpleadoAndFecha(Integer idEmpleado, LocalDate fecha);

    List<RegistroAsistenciaModel> findByFechaAndHoraSalidaIsNullAndTipoRegistro(LocalDate fecha, String tipoRegistro);

    List<RegistroAsistenciaModel> findByEmpleado_IdEmpleadoOrderByFechaDesc(Integer idEmpleado);
}
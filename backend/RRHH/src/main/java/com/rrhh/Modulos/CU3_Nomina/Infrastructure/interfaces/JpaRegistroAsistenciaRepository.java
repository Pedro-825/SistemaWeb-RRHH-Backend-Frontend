package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JpaRegistroAsistenciaRepository extends JpaRepository<RegistroAsistenciaModel, Integer> {

    List<RegistroAsistenciaModel> findByEmpleadoIdEmpleadoAndFechaBetween(
        Long idEmpleado,
        LocalDate fechaInicio,
        LocalDate fechaFin
    );

    List<RegistroAsistenciaModel> findByEmpleadoIdEmpleadoInAndFechaBetween(
        List<Long> idsEmpleados,
        LocalDate fechaInicio,
        LocalDate fechaFin
    );
}
package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.SancionModel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface JpaSancionRepository extends JpaRepository<SancionModel, Long> {

    List<SancionModel> findByEmpleadoIdEmpleadoAndEstado(
            Long idEmpleado,
            String estado
    );

    List<SancionModel> findByEmpleadoIdEmpleadoAndEstadoAndBloqueaAcceso(
            Long idEmpleado,
            String estado,
            Boolean bloqueaAcceso
    );

    List<SancionModel> findByEstadoAndBloqueaAccesoAndFechaFinLessThanEqual(
            String estado,
            Boolean bloqueaAcceso,
            LocalDate fechaFin
    );
}
package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JpaReporteAsistenciaRepository extends JpaRepository<RegistroAsistenciaModel, Integer> {

    @Query("SELECT r FROM RegistroAsistenciaModel r " +
           "JOIN ContratoModel c ON c.empleado = r.empleado AND c.estado = 'ACTIVO' " +
           "WHERE (:idEmpleado IS NULL OR r.empleado.idEmpleado = :idEmpleado) AND " +
                       "(:area IS NULL OR c.departamento.nombre = :area) AND " +
           "(:fechaInicio IS NULL OR r.fecha >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR r.fecha <= :fechaFin)")
    List<RegistroAsistenciaModel> buscarConFiltros(
        @Param("idEmpleado") Long idEmpleado,
        @Param("area") String area,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
}
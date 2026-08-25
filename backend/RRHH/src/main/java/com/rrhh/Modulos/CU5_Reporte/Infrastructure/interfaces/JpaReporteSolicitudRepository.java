package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.SolicitudModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JpaReporteSolicitudRepository extends JpaRepository<SolicitudModel, Integer> {

    @Query(value =
           // LEFT JOIN a proposito: una solicitud historica de un empleado ya
           // desvinculado/suspendido debe seguir apareciendo en el reporte.
           "SELECT s.* FROM solicitud s " +
           "LEFT JOIN contrato c ON c.id_empleado = s.id_empleado AND c.estado = 'ACTIVO' " +
           "LEFT JOIN departamento d ON d.id_dpto = c.id_dpto " +
           "WHERE (:idEmpleado IS NULL OR s.id_empleado = :idEmpleado) AND " +
           "(:area IS NULL OR d.nombre = :area) AND " +
           "(:tipoSolicitud IS NULL OR s.tipo_solicitud = :tipoSolicitud) AND " +
           "(:estado IS NULL OR s.estado = :estado) AND " +
           "(:cargo IS NULL OR c.cargo = :cargo) AND " +
           "(CAST(:fechaInicio AS DATE) IS NULL OR s.fecha_inicio >= CAST(:fechaInicio AS DATE)) AND " +
           "(CAST(:fechaFin AS DATE) IS NULL OR s.fecha_fin <= CAST(:fechaFin AS DATE)) AND " +
           "(:diasMin IS NULL OR s.dias_solicitados >= :diasMin) AND " +
           "(:diasMax IS NULL OR s.dias_solicitados <= :diasMax)",
           nativeQuery = true)
    List<SolicitudModel> buscarConFiltros(
        @Param("idEmpleado") Long idEmpleado,
        @Param("area") String area,
        @Param("tipoSolicitud") String tipoSolicitud,
        @Param("estado") String estado,
        @Param("cargo") String cargo,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("diasMin") Integer diasMin,
        @Param("diasMax") Integer diasMax
    );
}
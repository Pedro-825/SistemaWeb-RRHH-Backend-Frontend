package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaReporteEmpleadoRepository
        extends JpaRepository<EmpleadoModel, Long>, JpaSpecificationExecutor<EmpleadoModel> {
}

package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JpaReporteAsistenciaRepository extends JpaRepository<RegistroAsistenciaModel, Integer>,
        JpaSpecificationExecutor<RegistroAsistenciaModel> {
}

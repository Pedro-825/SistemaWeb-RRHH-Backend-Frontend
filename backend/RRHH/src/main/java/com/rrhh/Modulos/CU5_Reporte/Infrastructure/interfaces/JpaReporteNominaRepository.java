package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.NominaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JpaReporteNominaRepository extends JpaRepository<NominaModel, Integer>,
        JpaSpecificationExecutor<NominaModel> {

    List<NominaModel> findByPeriodo(String periodo);
}

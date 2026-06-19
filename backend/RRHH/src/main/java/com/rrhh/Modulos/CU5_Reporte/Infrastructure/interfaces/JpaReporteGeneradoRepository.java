package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.ReporteGeneradoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaReporteGeneradoRepository extends JpaRepository<ReporteGeneradoModel, Integer> {

    List<ReporteGeneradoModel> findByTipoReporte(String tipoReporte);
}
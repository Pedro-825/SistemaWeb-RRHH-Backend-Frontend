package com.rrhh.Modulos.CU5_Reporte.Domain.repository;

import com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado;

import java.util.List;

public interface IReporteGeneradoRepository {

    ReporteGenerado guardar(ReporteGenerado reporte);

    List<ReporteGenerado> buscarTodos();

    List<ReporteGenerado> buscarPorTipo(String tipoReporte);
}
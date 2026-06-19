package com.rrhh.Modulos.CU5_Reporte.Application.services;

import com.rrhh.Modulos.CU5_Reporte.Application.dto.*;

import java.util.List;
import java.util.Map;

public interface IReporteService {

    ReporteResponseDTO generarReporteEmpleados(FiltroEmpleadoDTO filtro, Long idUsuario);

    ReporteResponseDTO generarReporteAsistencia(FiltroAsistenciaDTO filtro, Long idUsuario);

    ReporteResponseDTO generarReporteNomina(FiltroNominaDTO filtro, Long idUsuario);

    ReporteResponseDTO generarReporteSolicitudes(FiltroSolicitudDTO filtro, Long idUsuario);

    byte[] exportarReporte(String tipo, String formato, List<Map<String, Object>> datos);
}

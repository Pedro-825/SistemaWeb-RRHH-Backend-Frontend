package com.rrhh.Modulos.CU5_Reporte.Application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReporteResponseDTO {

    private Integer idReporte;
    private String tipoReporte;
    private Integer totalRegistros;
    private LocalDateTime generadoEl;
    private List<Map<String, Object>> datos;

    public ReporteResponseDTO() {}

    public Integer getIdReporte() { return idReporte; }
    public void setIdReporte(Integer idReporte) { this.idReporte = idReporte; }

    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }

    public Integer getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }

    public LocalDateTime getGeneradoEl() { return generadoEl; }
    public void setGeneradoEl(LocalDateTime generadoEl) { this.generadoEl = generadoEl; }

    public List<Map<String, Object>> getDatos() { return datos; }
    public void setDatos(List<Map<String, Object>> datos) { this.datos = datos; }
}
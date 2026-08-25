package com.rrhh.Modulos.CU5_Reporte.Application.dto;

import java.time.LocalDate;

public class FiltroAsistenciaDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String area;
    private Long idEmpleado;
    private String estado;
    private String tipoRegistro;
    private Integer minutosTardanzaMin;

    public FiltroAsistenciaDTO() {}

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoRegistro() { return tipoRegistro; }
    public void setTipoRegistro(String tipoRegistro) { this.tipoRegistro = tipoRegistro; }

    public Integer getMinutosTardanzaMin() { return minutosTardanzaMin; }
    public void setMinutosTardanzaMin(Integer minutosTardanzaMin) { this.minutosTardanzaMin = minutosTardanzaMin; }
}
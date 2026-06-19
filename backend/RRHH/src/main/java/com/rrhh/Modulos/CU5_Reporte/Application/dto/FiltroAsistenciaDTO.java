package com.rrhh.Modulos.CU5_Reporte.Application.dto;

import java.time.LocalDate;

public class FiltroAsistenciaDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String area;
    private Long idEmpleado;

    public FiltroAsistenciaDTO() {}

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
}
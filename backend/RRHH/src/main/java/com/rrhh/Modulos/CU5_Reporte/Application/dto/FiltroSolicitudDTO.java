package com.rrhh.Modulos.CU5_Reporte.Application.dto;

import java.time.LocalDate;

public class FiltroSolicitudDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String area;
    private Long idEmpleado;
    private String tipoSolicitud;
    private String estado;
    private String cargo;
    private Integer diasMin;
    private Integer diasMax;

    public FiltroSolicitudDTO() {}

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public Integer getDiasMin() { return diasMin; }
    public void setDiasMin(Integer diasMin) { this.diasMin = diasMin; }

    public Integer getDiasMax() { return diasMax; }
    public void setDiasMax(Integer diasMax) { this.diasMax = diasMax; }
}
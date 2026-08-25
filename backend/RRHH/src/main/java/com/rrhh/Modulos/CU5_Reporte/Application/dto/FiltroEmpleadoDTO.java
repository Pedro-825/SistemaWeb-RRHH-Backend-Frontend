package com.rrhh.Modulos.CU5_Reporte.Application.dto;

import java.time.LocalDate;

public class FiltroEmpleadoDTO {

    private String area;
    private String cargo;
    private String tipoContrato;
    private String estado;
    private Long idEmpleado;
    private LocalDate fechaIngresoInicio;
    private LocalDate fechaIngresoFin;
    private String rol;

    public FiltroEmpleadoDTO() {}

    public LocalDate getFechaIngresoInicio() { return fechaIngresoInicio; }
    public void setFechaIngresoInicio(LocalDate fechaIngresoInicio) { this.fechaIngresoInicio = fechaIngresoInicio; }

    public LocalDate getFechaIngresoFin() { return fechaIngresoFin; }
    public void setFechaIngresoFin(LocalDate fechaIngresoFin) { this.fechaIngresoFin = fechaIngresoFin; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
}
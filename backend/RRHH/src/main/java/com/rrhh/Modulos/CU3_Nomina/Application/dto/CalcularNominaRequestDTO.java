package com.rrhh.Modulos.CU3_Nomina.Application.dto;

import java.time.LocalDate;

public class CalcularNominaRequestDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long idEmpleado;
    private Integer idDepartamento;

    public CalcularNominaRequestDTO() {}

    public CalcularNominaRequestDTO(LocalDate fechaInicio, LocalDate fechaFin,
                                     Long idEmpleado, Integer idDepartamento) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.idEmpleado = idEmpleado;
        this.idDepartamento = idDepartamento;
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }
}
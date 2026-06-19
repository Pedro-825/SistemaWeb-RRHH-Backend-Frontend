package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistrarSancionRequestDTO {

    @NotBlank(message = "El motivo de la sancion es obligatorio")
    private String motivo;
    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;
    private Boolean bloquearAcceso;

    public RegistrarSancionRequestDTO() {
    }

    public RegistrarSancionRequestDTO(
            String motivo,
            String descripcion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Boolean bloquearAcceso
    ) {
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.bloquearAcceso = bloquearAcceso;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getBloquearAcceso() {
        return bloquearAcceso;
    }

    public void setBloquearAcceso(Boolean bloquearAcceso) {
        this.bloquearAcceso = bloquearAcceso;
    }
}
package com.rrhh.Modulos.CU6_Solicitud.Application.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistrarSolicitudRequestDTO {

    @NotBlank(message = "Debe completar todos los campos obligatorios.")
    private String tipoSolicitud;

    @NotNull(message = "Debe completar todos los campos obligatorios.")
    private LocalDate fechaInicio;

    @NotNull(message = "Debe completar todos los campos obligatorios.")
    private LocalDate fechaFin;

    @NotBlank(message = "Debe completar todos los campos obligatorios.")
    private String motivo;
    private String archivoAdjunto;

    public RegistrarSolicitudRequestDTO() {}

    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getArchivoAdjunto() { return archivoAdjunto; }
    public void setArchivoAdjunto(String archivoAdjunto) { this.archivoAdjunto = archivoAdjunto; }
}
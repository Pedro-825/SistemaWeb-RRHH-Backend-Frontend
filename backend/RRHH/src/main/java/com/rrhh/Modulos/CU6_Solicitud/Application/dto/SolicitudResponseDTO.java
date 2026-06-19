package com.rrhh.Modulos.CU6_Solicitud.Application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SolicitudResponseDTO {

    private Integer idSolicitud;
    private String tipoSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer diasSolicitados;
    private String motivo;
    private String estado;
    private String respuesta;
    private String observacionRrhh;
    private String archivoAdjunto;
    private LocalDateTime fechaRevision;
    private LocalDateTime fechaDecision;
    private LocalDateTime creadoEl;
    private Long idEmpleado;
    private String nombreEmpleado;

    public SolicitudResponseDTO() {}

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getDiasSolicitados() { return diasSolicitados; }
    public void setDiasSolicitados(Integer diasSolicitados) { this.diasSolicitados = diasSolicitados; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public String getObservacionRrhh() { return observacionRrhh; }
    public void setObservacionRrhh(String observacionRrhh) { this.observacionRrhh = observacionRrhh; }

    public String getArchivoAdjunto() { return archivoAdjunto; }
    public void setArchivoAdjunto(String archivoAdjunto) { this.archivoAdjunto = archivoAdjunto; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }

    public LocalDateTime getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(LocalDateTime fechaDecision) { this.fechaDecision = fechaDecision; }

    public LocalDateTime getCreadoEl() { return creadoEl; }
    public void setCreadoEl(LocalDateTime creadoEl) { this.creadoEl = creadoEl; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
}
package com.rrhh.Modulos.CU6_Solicitud.Domain.entities;

import com.rrhh.Shared.prototype.Prototype;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
public class Solicitud implements Prototype<Solicitud> {

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
    private LocalDateTime actualizadoEl;

    private Long idEmpleado;
    private Long revisadoPor;
    private Long aprobadoPor;

    public Solicitud() {}

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

    public LocalDateTime getActualizadoEl() { return actualizadoEl; }
    public void setActualizadoEl(LocalDateTime actualizadoEl) { this.actualizadoEl = actualizadoEl; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Long getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(Long revisadoPor) { this.revisadoPor = revisadoPor; }

    public Long getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Long aprobadoPor) { this.aprobadoPor = aprobadoPor; }

    @Override
    public Solicitud clonePrototype() {
        Solicitud copia = new Solicitud();
        copia.setIdSolicitud(this.idSolicitud);
        copia.setTipoSolicitud(this.tipoSolicitud);
        copia.setFechaInicio(this.fechaInicio);
        copia.setFechaFin(this.fechaFin);
        copia.setDiasSolicitados(this.diasSolicitados);
        copia.setMotivo(this.motivo);
        copia.setEstado(this.estado);
        copia.setRespuesta(this.respuesta);
        copia.setObservacionRrhh(this.observacionRrhh);
        copia.setArchivoAdjunto(this.archivoAdjunto);
        copia.setFechaRevision(this.fechaRevision);
        copia.setFechaDecision(this.fechaDecision);
        copia.setCreadoEl(this.creadoEl);
        copia.setActualizadoEl(this.actualizadoEl);
        copia.setIdEmpleado(this.idEmpleado);
        copia.setRevisadoPor(this.revisadoPor);
        copia.setAprobadoPor(this.aprobadoPor);
        return copia;
    }
}
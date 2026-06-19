package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "justificacion_tardanza")
public class JustificacionTardanzaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_justificacion")
    private Integer idJustificacion;

    @Column(name = "id_registro_asistencia", nullable = false)
    private Integer idRegistroAsistencia;

    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Column(name = "fecha_justificacion", nullable = false)
    private LocalDateTime fechaJustificacion;

    @Column(name = "revisado_por")
    private Long revisadoPor;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "comentario_revision", length = 500)
    private String comentarioRevision;

    @Transient
    private String horaEntrada;

    @Transient
    private String fechaAsistencia;

    public JustificacionTardanzaModel() {}

    public Integer getIdJustificacion() { return idJustificacion; }
    public void setIdJustificacion(Integer idJustificacion) { this.idJustificacion = idJustificacion; }

    public Integer getIdRegistroAsistencia() { return idRegistroAsistencia; }
    public void setIdRegistroAsistencia(Integer idRegistroAsistencia) { this.idRegistroAsistencia = idRegistroAsistencia; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEvidenciaUrl() { return evidenciaUrl; }
    public void setEvidenciaUrl(String evidenciaUrl) { this.evidenciaUrl = evidenciaUrl; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaJustificacion() { return fechaJustificacion; }
    public void setFechaJustificacion(LocalDateTime fechaJustificacion) { this.fechaJustificacion = fechaJustificacion; }

    public Long getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(Long revisadoPor) { this.revisadoPor = revisadoPor; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }

    public String getComentarioRevision() { return comentarioRevision; }
    public void setComentarioRevision(String comentarioRevision) { this.comentarioRevision = comentarioRevision; }

    public String getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(String horaEntrada) { this.horaEntrada = horaEntrada; }

    public String getFechaAsistencia() { return fechaAsistencia; }
    public void setFechaAsistencia(String fechaAsistencia) { this.fechaAsistencia = fechaAsistencia; }
}

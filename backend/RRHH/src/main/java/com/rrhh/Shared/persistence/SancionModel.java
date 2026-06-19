package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sancion")
public class SancionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sancion")
    private Long idSancion;

    @Column(name = "codigo", unique = true, length = 30)
    private String codigo;

    @Column(name = "motivo", nullable = false, length = 150)
    private String motivo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "bloquea_acceso")
    private Boolean bloqueaAcceso = false;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVA";

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    // =========================
    // RELACIONES
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", referencedColumnName = "id_empleado", nullable = false)
    private EmpleadoModel empleado;

    // =========================
    // CONSTRUCTORES
    // =========================

    public SancionModel() {
    }

    public SancionModel(
            Long idSancion,
            String motivo,
            String descripcion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Boolean bloqueaAcceso,
            String estado,
            LocalDateTime creadoEl,
            LocalDateTime actualizadoEl,
            EmpleadoModel empleado
    ) {
        this.idSancion = idSancion;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.bloqueaAcceso = bloqueaAcceso;
        this.estado = estado;
        this.creadoEl = creadoEl;
        this.actualizadoEl = actualizadoEl;
        this.empleado = empleado;
    }

    @PrePersist
    public void prePersist() {
        this.creadoEl = LocalDateTime.now();
        this.actualizadoEl = LocalDateTime.now();

        if (this.bloqueaAcceso == null) {
            this.bloqueaAcceso = false;
        }

        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "ACTIVA";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEl = LocalDateTime.now();
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public Long getIdSancion() {
        return idSancion;
    }

    public void setIdSancion(Long idSancion) {
        this.idSancion = idSancion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Boolean getBloqueaAcceso() {
        return bloqueaAcceso;
    }

    public void setBloqueaAcceso(Boolean bloqueaAcceso) {
        this.bloqueaAcceso = bloqueaAcceso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEl() {
        return creadoEl;
    }

    public void setCreadoEl(LocalDateTime creadoEl) {
        this.creadoEl = creadoEl;
    }

    public LocalDateTime getActualizadoEl() {
        return actualizadoEl;
    }

    public void setActualizadoEl(LocalDateTime actualizadoEl) {
        this.actualizadoEl = actualizadoEl;
    }

    public EmpleadoModel getEmpleado() {
        return empleado;
    }

    public void setEmpleado(EmpleadoModel empleado) {
        this.empleado = empleado;
    }
}
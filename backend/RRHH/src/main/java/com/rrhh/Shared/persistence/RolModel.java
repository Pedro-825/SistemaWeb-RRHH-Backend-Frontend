package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rol")
public class RolModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre_rol", unique = true, nullable = false)
    private String nombreRol;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl;

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl;

    @Column(name = "activo")
    private Boolean activo = true;

    @PrePersist
    public void prePersist() {
        this.creadoEl = LocalDateTime.now();
        this.actualizadoEl = LocalDateTime.now();

        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEl = LocalDateTime.now();
    }

    public RolModel() {
    }

    public RolModel(
            Integer idRol,
            String nombreRol,
            LocalDateTime creadoEl,
            LocalDateTime actualizadoEl,
            Boolean activo
    ) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.creadoEl = creadoEl;
        this.actualizadoEl = actualizadoEl;
        this.activo = activo;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
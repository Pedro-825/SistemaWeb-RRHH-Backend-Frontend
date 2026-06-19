package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities;

import java.time.LocalDate;

public class Sancion {

    private Long idSancion;
    private String codigo;
    private Long idEmpleado;
    private String motivo;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean bloqueaAcceso;
    private String estado;

    public Sancion() {
    }

    public boolean estaActiva() {
        return "ACTIVA".equalsIgnoreCase(this.estado);
    }

    public boolean bloqueaAcceso() {
        return Boolean.TRUE.equals(this.bloqueaAcceso);
    }

    public boolean estaVencida(LocalDate fechaActual) {
        return fechaFin != null && !fechaFin.isAfter(fechaActual);
    }

    public void finalizar() {
        this.estado = "FINALIZADA";
    }

    public void anular() {
        this.estado = "ANULADA";
    }

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

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
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
}
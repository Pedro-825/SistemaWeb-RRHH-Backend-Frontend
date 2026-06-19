package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrato")
public class ContratoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Long idContrato;

    @Column(name = "cargo", nullable = false, length = 100)
    private String cargo;

    @Column(name = "tipo_contrato", nullable = false, length = 50)
    private String tipoContrato;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "sueldo", nullable = false, precision = 10, scale = 2)
    private BigDecimal sueldo;

    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";

    @Column(name = "tipo_pension", length = 10)
    private String tipoPension = "ONP";

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", referencedColumnName = "id_empleado", nullable = false)
    private EmpleadoModel empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dpto", referencedColumnName = "id_dpto", nullable = false)
    private DepartamentoModel departamento;

    public ContratoModel() {
    }

    public Long getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Long idContrato) {
        this.idContrato = idContrato;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
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

    public BigDecimal getSueldo() {
        return sueldo;
    }

    public void setSueldo(BigDecimal sueldo) {
        this.sueldo = sueldo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoPension() {
        return tipoPension;
    }

    public void setTipoPension(String tipoPension) {
        this.tipoPension = tipoPension;
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

    public DepartamentoModel getDepartamento() {
        return departamento;
    }

    public void setDepartamento(DepartamentoModel departamento) {
        this.departamento = departamento;
    }
}
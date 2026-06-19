package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rrhh.Shared.prototype.Prototype;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contrato implements Prototype<Contrato> {

    private Long idContrato;
    private String cargo;
    private String tipoContrato;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal sueldo;
    private String estado;
    private Long idEmpleado;
    private Integer idDpto;
    private String nombreDepartamento;

    public boolean estaActivo() {
        return "ACTIVO".equalsIgnoreCase(this.estado);
    }

    public void cambiarCargo(String nuevoCargo) {
        this.cargo = nuevoCargo;
    }

    public void cambiarDepartamento(Integer nuevoIdDpto, String nuevoNombreDepartamento) {
        this.idDpto = nuevoIdDpto;
        this.nombreDepartamento = nuevoNombreDepartamento;
    }

    public void cambiarSueldo(BigDecimal nuevoSueldo) {
        if (nuevoSueldo == null || nuevoSueldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El sueldo debe ser mayor a 0");
        }

        this.sueldo = nuevoSueldo;
    }

    public void resolver() {
        this.estado = "RESUELTO";
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

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdDpto() {
        return idDpto;
    }

    public void setIdDpto(Integer idDpto) {
        this.idDpto = idDpto;
    }

    public String getNombreDepartamento() {
        return nombreDepartamento;
    }

    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento;
    }

    @Override
    public Contrato clonePrototype() {
        Contrato clon = new Contrato();
        clon.setCargo(this.cargo);
        clon.setTipoContrato(this.tipoContrato);
        clon.setSueldo(this.sueldo);
        clon.setEstado(this.estado);
        clon.setIdDpto(this.idDpto);
        clon.setNombreDepartamento(this.nombreDepartamento);
        return clon;
    }
}
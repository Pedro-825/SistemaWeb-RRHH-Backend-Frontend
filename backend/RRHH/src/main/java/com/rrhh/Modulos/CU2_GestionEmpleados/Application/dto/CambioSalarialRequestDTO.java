package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CambioSalarialRequestDTO {

    private BigDecimal nuevoSueldo;
    private LocalDate fechaCambio;
    private String motivo;

    public CambioSalarialRequestDTO() {
    }

    public CambioSalarialRequestDTO(
            BigDecimal nuevoSueldo,
            LocalDate fechaCambio,
            String motivo
    ) {
        this.nuevoSueldo = nuevoSueldo;
        this.fechaCambio = fechaCambio;
        this.motivo = motivo;
    }

    public BigDecimal getNuevoSueldo() {
        return nuevoSueldo;
    }

    public void setNuevoSueldo(BigDecimal nuevoSueldo) {
        this.nuevoSueldo = nuevoSueldo;
    }

    public LocalDate getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDate fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
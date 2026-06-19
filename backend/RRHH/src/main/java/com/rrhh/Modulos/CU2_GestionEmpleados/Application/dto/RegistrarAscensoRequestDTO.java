package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import java.time.LocalDate;

public class RegistrarAscensoRequestDTO {

    private String nuevoCargo;
    private Integer nuevoIdDpto;
    private LocalDate fechaAscenso;
    private String motivo;

    public RegistrarAscensoRequestDTO() {
    }

    public RegistrarAscensoRequestDTO(
            String nuevoCargo,
            Integer nuevoIdDpto,
            LocalDate fechaAscenso,
            String motivo
    ) {
        this.nuevoCargo = nuevoCargo;
        this.nuevoIdDpto = nuevoIdDpto;
        this.fechaAscenso = fechaAscenso;
        this.motivo = motivo;
    }

    public String getNuevoCargo() {
        return nuevoCargo;
    }

    public void setNuevoCargo(String nuevoCargo) {
        this.nuevoCargo = nuevoCargo;
    }

    public Integer getNuevoIdDpto() {
        return nuevoIdDpto;
    }

    public void setNuevoIdDpto(Integer nuevoIdDpto) {
        this.nuevoIdDpto = nuevoIdDpto;
    }

    public LocalDate getFechaAscenso() {
        return fechaAscenso;
    }

    public void setFechaAscenso(LocalDate fechaAscenso) {
        this.fechaAscenso = fechaAscenso;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
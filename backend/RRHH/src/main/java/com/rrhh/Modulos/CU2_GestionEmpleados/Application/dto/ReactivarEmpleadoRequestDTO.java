package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

public class ReactivarEmpleadoRequestDTO {

    private String motivo;

    public ReactivarEmpleadoRequestDTO() {
    }

    public ReactivarEmpleadoRequestDTO(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
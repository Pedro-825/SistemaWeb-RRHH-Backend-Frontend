package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import jakarta.validation.constraints.NotBlank;

public class RegistrarSancionRequestDTO {

    @NotBlank(message = "El tipo de sanción es obligatorio")
    private String tipoSancion;
    private Integer diasSuspension;

    @NotBlank(message = "La justificación es obligatoria")
    private String justificacion;

    public RegistrarSancionRequestDTO() {
    }

    public RegistrarSancionRequestDTO(
            String tipoSancion,
            Integer diasSuspension,
            String justificacion
    ) {
        this.tipoSancion = tipoSancion;
        this.diasSuspension = diasSuspension;
        this.justificacion = justificacion;
    }

    public String getTipoSancion() {
        return tipoSancion;
    }

    public void setTipoSancion(String tipoSancion) {
        this.tipoSancion = tipoSancion;
    }

    public Integer getDiasSuspension() {
        return diasSuspension != null ? diasSuspension : 0;
    }

    public void setDiasSuspension(Integer diasSuspension) {
        this.diasSuspension = diasSuspension;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }
}

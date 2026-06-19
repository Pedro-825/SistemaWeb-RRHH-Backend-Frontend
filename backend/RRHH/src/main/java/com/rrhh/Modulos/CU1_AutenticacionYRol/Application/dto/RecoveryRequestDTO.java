package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

import jakarta.validation.constraints.NotBlank;

public class RecoveryRequestDTO {

    @NotBlank(message = "El usuario o correo es obligatorio")
    private String identifier;

    public RecoveryRequestDTO() {
    }

    public RecoveryRequestDTO(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}

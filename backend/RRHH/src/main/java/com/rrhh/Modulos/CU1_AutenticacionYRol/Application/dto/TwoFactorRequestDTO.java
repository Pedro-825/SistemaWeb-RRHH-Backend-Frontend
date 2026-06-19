package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

import jakarta.validation.constraints.NotBlank;

public class TwoFactorRequestDTO {
    @NotBlank(message = "El username es obligatorio")
    private String username;
    @NotBlank(message = "El codigo 2FA es obligatorio")
    private String code;

    public String getUsername() { return username; }
    public String getCode() { return code; }

    public void setUsername(String username) { this.username = username; }
    public void setCode(String code) { this.code = code; }
}
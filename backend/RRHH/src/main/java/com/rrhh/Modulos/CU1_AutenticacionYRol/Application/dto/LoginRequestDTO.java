package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;
    @NotBlank(message = "La contrasenia es obligatoria")
    private String password;

    public LoginRequestDTO() {
    }

    // Constructor con parámetros
    public LoginRequestDTO(String nombreUsuario, String password) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

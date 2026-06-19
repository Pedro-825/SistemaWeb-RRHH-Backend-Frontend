package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequestDTO {

    @NotBlank(message = "El token es obligatorio")
    private String token;
    @NotBlank(message = "La contrasenia es obligatoria")
    @Size(min = 8, message = "La contrasenia debe tener al menos 8 caracteres")
    private String nuevaContrasenia;
    @NotBlank(message = "Debe confirmar la contrasenia")
    private String confirmarContrasenia;

    public ResetPasswordRequestDTO() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNuevaContrasenia() {
        return nuevaContrasenia;
    }

    public void setNuevaContrasenia(String nuevaContrasenia) {
        this.nuevaContrasenia = nuevaContrasenia;
    }

    public String getConfirmarContrasenia() {
        return confirmarContrasenia;
    }

    public void setConfirmarContrasenia(String confirmarContrasenia) {
        this.confirmarContrasenia = confirmarContrasenia;
    }
}
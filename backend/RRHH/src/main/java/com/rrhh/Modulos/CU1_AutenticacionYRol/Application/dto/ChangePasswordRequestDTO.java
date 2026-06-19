package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequestDTO {

    @NotBlank(message = "La contrasenia actual es obligatoria")
    private String oldPassword;

    @NotBlank(message = "La nueva contrasenia es obligatoria")
    @Size(min = 8, message = "La contrasenia debe tener al menos 8 caracteres")
    private String newPassword;

    @NotBlank(message = "Debe confirmar la nueva contrasenia")
    private String confirmPassword;

    public ChangePasswordRequestDTO() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

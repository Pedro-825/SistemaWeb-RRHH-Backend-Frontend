
package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

public class TwoFactorResponseDTO {

    private boolean success;
    private String message;
    private String token;
    private String rol;
    private boolean requiereCambioPassword;

    public TwoFactorResponseDTO() {
    }

    public TwoFactorResponseDTO(
            boolean success,
            String message,
            String token,
            String rol
    ) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.rol = rol;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isRequiereCambioPassword() {
        return requiereCambioPassword;
    }

    public void setRequiereCambioPassword(boolean requiereCambioPassword) {
        this.requiereCambioPassword = requiereCambioPassword;
    }
}

package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

public class TwoFactorResponseDTO {

    private boolean success;
    private String message;
    private String token;
    private String rol;
    private boolean requiereCambioPassword;
    private Long idEmpleado;
    private String nombreUsuario;
    private boolean appMovilInstalada;

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

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public boolean isAppMovilInstalada() { return appMovilInstalada; }
    public void setAppMovilInstalada(boolean appMovilInstalada) { this.appMovilInstalada = appMovilInstalada; }
}
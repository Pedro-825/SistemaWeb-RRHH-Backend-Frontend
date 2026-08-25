package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

public class AuthResponseDTO {
    private boolean success;
    private String message;
    private String rol;
    private boolean requires2FA;
    private String token;
    private boolean requires2FASetup;
    private boolean requiereCambioPassword;
    private boolean dosFaYaConfigurado;
    private Long idEmpleado;
    private boolean appMovilInstalada;
    private String nombreUsuario;

    public AuthResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponseDTO(boolean success, String message, String rol, boolean requires2FA) {
        this.success = success;
        this.message = message;
        this.rol = rol;
        this.requires2FA = requires2FA;
    }

    public AuthResponseDTO(boolean success, String message, String rol,
                           boolean requires2FA, String token, boolean requires2FASetup) {
        this.success = success;
        this.message = message;
        this.rol = rol;
        this.requires2FA = requires2FA;
        this.token = token;
        this.requires2FASetup = requires2FASetup;
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isRequires2FA() {
        return requires2FA;
    }

    public void setRequires2FA(boolean requires2fa) {
        this.requires2FA = requires2fa;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isRequires2FASetup() {
        return requires2FASetup;
    }

    public void setRequires2FASetup(boolean requires2FASetup) {
        this.requires2FASetup = requires2FASetup;
    }

    public boolean isRequiereCambioPassword() {
        return requiereCambioPassword;
    }

    public void setRequiereCambioPassword(boolean requiereCambioPassword) {
        this.requiereCambioPassword = requiereCambioPassword;
    }

    public boolean isDosFaYaConfigurado() { return dosFaYaConfigurado; }
    public void setDosFaYaConfigurado(boolean dosFaYaConfigurado) { this.dosFaYaConfigurado = dosFaYaConfigurado; }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public boolean isAppMovilInstalada() { return appMovilInstalada; }
    public void setAppMovilInstalada(boolean appMovilInstalada) { this.appMovilInstalada = appMovilInstalada; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}

package com.rrhh.config.security;

public class AuthenticatedUser {

    private Long idUsuario;
    private String username;
    private String rol;

    public AuthenticatedUser(
            Long idUsuario,
            String username,
            String rol
    ) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.rol = rol;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public String getRol() {
        return rol;
    }
}
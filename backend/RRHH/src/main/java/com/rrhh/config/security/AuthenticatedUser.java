package com.rrhh.config.security;

public class AuthenticatedUser {

    private Long idUsuario;
    private Long idEmpleado;
    private String username;
    private String rol;

    public AuthenticatedUser(
            Long idUsuario,
            Long idEmpleado,
            String username,
            String rol
    ) {
        this.idUsuario = idUsuario;
        this.idEmpleado = idEmpleado;
        this.username = username;
        this.rol = rol;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public String getUsername() {
        return username;
    }

    public String getRol() {
        return rol;
    }
}

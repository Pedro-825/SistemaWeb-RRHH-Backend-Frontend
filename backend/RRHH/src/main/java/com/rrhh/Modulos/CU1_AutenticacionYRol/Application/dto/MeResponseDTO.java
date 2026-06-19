package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

public class MeResponseDTO {
    private String username;
    private String rol;
    private Long idEmpleado;

    public MeResponseDTO(String username, String rol, Long idEmpleado) {
        this.username = username;
        this.rol = rol;
        this.idEmpleado = idEmpleado;
    }

    public String getUsername() { return username; }
    public String getRol() { return rol; }
    public Long getIdEmpleado() { return idEmpleado; }
}

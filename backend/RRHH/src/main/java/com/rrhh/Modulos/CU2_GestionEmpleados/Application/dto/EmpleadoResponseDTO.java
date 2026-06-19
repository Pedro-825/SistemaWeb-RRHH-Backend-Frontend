package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

public class EmpleadoResponseDTO {

    private boolean success;
    private String message;
    private Long idEmpleado;
    private String nombreCompleto;
    private String nombreUsuario;
    private String correoInstitucional;
    private String cargo;
    private String departamento;
    private String rolAsignado;
    private String estado;

    public EmpleadoResponseDTO() {
    }

    public EmpleadoResponseDTO(
            boolean success,
            String message,
            Long idEmpleado,
            String nombreCompleto,
            String nombreUsuario,
            String correoInstitucional,
            String cargo,
            String departamento,
            String rolAsignado,
            String estado
    ) {
        this.success = success;
        this.message = message;
        this.idEmpleado = idEmpleado;
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario =nombreUsuario;
        this.correoInstitucional =correoInstitucional;
        this.cargo = cargo;
        this.departamento = departamento;
        this.rolAsignado = rolAsignado;
        this.estado = estado;
        
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

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getRolAsignado() {
        return rolAsignado;
    }

    public void setRolAsignado(String rolAsignado) {
        this.rolAsignado = rolAsignado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}


   
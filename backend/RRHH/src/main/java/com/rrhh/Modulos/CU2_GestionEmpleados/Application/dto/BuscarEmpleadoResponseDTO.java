package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BuscarEmpleadoResponseDTO {

    private Long idEmpleado;
    private String nombres;
    private String apellidos;
    private String numeroDi;
    private String correo;
    private String telefono;
    private String estado;

    private String cargo;
    private String departamento;
    private String rol;
    private String nombreUsuario;
    private String correoInstitucional;
    private BigDecimal sueldo;
    private LocalDate fechaInicio;
    private String tipoContrato;
    private Integer idDpto;
    private String direccion;

    public BuscarEmpleadoResponseDTO() {
    }

    public BuscarEmpleadoResponseDTO(
            Long idEmpleado,
            String nombres,
            String apellidos,
            String numeroDi,
            String correo,
            String telefono,
            String estado,
            String cargo,
            String departamento,
            String rol,
            String nombreUsuario,
            String correoInstitucional,
            BigDecimal sueldo,
            LocalDate fechaInicio,
            String tipoContrato,
            Integer idDpto,
            String direccion
    ) {
        this.idEmpleado = idEmpleado;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.numeroDi = numeroDi;
        this.correo = correo;
        this.telefono = telefono;
        this.estado = estado;
        this.cargo = cargo;
        this.departamento = departamento;
        this.rol = rol;
        this.nombreUsuario = nombreUsuario;
        this.correoInstitucional = correoInstitucional;
        this.sueldo = sueldo;
        this.fechaInicio = fechaInicio;
        this.tipoContrato = tipoContrato;
        this.idDpto = idDpto;
        this.direccion = direccion;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNumeroDi() {
        return numeroDi;
    }

    public void setNumeroDi(String numeroDi) {
        this.numeroDi = numeroDi;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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

    public java.math.BigDecimal getSueldo() {
        return sueldo;
    }

    public void setSueldo(java.math.BigDecimal sueldo) {
        this.sueldo = sueldo;
    }

    public java.time.LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(java.time.LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }

    public Integer getIdDpto() { return idDpto; }
    public void setIdDpto(Integer idDpto) { this.idDpto = idDpto; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
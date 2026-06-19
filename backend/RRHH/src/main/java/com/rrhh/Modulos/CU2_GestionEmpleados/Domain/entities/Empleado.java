package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Empleado {

    private Long idEmpleado;
    private String nombres;
    private String apellidos;
    private String docIdentidad;
    private String numeroDi;
    private LocalDate fechaNac;
    private String sexo;
    private String estadoCivil;
    private String direccion;
    private String correo;
    private String telefono;
    private String estado;
    private LocalDateTime fechaDesvinculacion;

    public Empleado() {
    }

    public boolean estaDesvinculado() {
        return "DESVINCULADO".equalsIgnoreCase(this.estado);
    }

    public boolean estaSuspendido() {
        return "SUSPENDIDO".equalsIgnoreCase(this.estado);
    }

    public boolean estaActivo() {
        return "ACTIVO".equalsIgnoreCase(this.estado);
    }

    public boolean puedeSerEditado() {
        return !estaDesvinculado();
    }

    public boolean puedeSerSancionado() {
        return !estaDesvinculado();
    }

    public boolean puedeRecibirAscenso() {
        return estaActivo();
    }

    public boolean puedeRecibirCambioSalarial() {
        return estaActivo();
    }

    public void desvincular() {
        this.estado = "DESVINCULADO";
        this.fechaDesvinculacion = LocalDateTime.now();
    }

    public void suspender() {
        this.estado = "SUSPENDIDO";
    }

    public void activar() {
        this.estado = "ACTIVO";
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

    public String getDocIdentidad() {
        return docIdentidad;
    }

    public void setDocIdentidad(String docIdentidad) {
        this.docIdentidad = docIdentidad;
    }

    public String getNumeroDi() {
        return numeroDi;
    }

    public void setNumeroDi(String numeroDi) {
        this.numeroDi = numeroDi;
    }

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public LocalDateTime getFechaDesvinculacion() {
        return fechaDesvinculacion;
    }

    public void setFechaDesvinculacion(LocalDateTime fechaDesvinculacion) {
        this.fechaDesvinculacion = fechaDesvinculacion;
    }
}
package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;

public class ActualizarEmpleadoRequestDTO {

    private LocalDate fechaNac;
    private String sexo;
    private String estadoCivil;
    private String direccion;

    @Email(message = "El correo electronico no tiene un formato valido")
    private String correo;
    private String telefono;

    private String cargo;
    private BigDecimal sueldo;
    private Integer idDpto;
    private String tipoContrato;

    public ActualizarEmpleadoRequestDTO() {
    }

    public ActualizarEmpleadoRequestDTO(
            LocalDate fechaNac,
            String sexo,
            String estadoCivil,
            String direccion,
            String correo,
            String telefono
    ) {
        this.fechaNac = fechaNac;
        this.sexo = sexo;
        this.estadoCivil = estadoCivil;
        this.direccion = direccion;
        this.correo = correo;
        this.telefono = telefono;
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

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public BigDecimal getSueldo() {
        return sueldo;
    }

    public void setSueldo(BigDecimal sueldo) {
        this.sueldo = sueldo;
    }

    public Integer getIdDpto() {
        return idDpto;
    }

    public void setIdDpto(Integer idDpto) {
        this.idDpto = idDpto;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }
}
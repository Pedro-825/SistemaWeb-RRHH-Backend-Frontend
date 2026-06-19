package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities;

public class Rol {

    private Integer idRol;
    private String nombreRol;
    private Boolean activo;

    public Rol() {
    }

    public boolean estaActivo() {
        return activo != null && activo;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
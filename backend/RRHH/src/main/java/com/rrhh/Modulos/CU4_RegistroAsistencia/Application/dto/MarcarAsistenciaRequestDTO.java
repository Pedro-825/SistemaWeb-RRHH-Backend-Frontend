package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto;

public class MarcarAsistenciaRequestDTO {

    private Integer idEmpleado;
    private String huellaTemplate;

    public MarcarAsistenciaRequestDTO() {
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getHuellaTemplate() {
        return huellaTemplate;
    }

    public void setHuellaTemplate(String huellaTemplate) {
        this.huellaTemplate = huellaTemplate;
    }
}
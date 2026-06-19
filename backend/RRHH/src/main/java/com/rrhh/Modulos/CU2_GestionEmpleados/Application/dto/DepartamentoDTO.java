package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

public class DepartamentoDTO {

    private Integer idDpto;
    private String nombre;
    private String codDpto;

    public DepartamentoDTO() {}

    public DepartamentoDTO(Integer idDpto, String nombre, String codDpto) {
        this.idDpto = idDpto;
        this.nombre = nombre;
        this.codDpto = codDpto;
    }

    public Integer getIdDpto() { return idDpto; }
    public void setIdDpto(Integer idDpto) { this.idDpto = idDpto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodDpto() { return codDpto; }
    public void setCodDpto(String codDpto) { this.codDpto = codDpto; }
}

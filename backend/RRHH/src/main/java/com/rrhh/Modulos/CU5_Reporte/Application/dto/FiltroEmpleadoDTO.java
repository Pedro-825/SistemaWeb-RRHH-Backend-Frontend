package com.rrhh.Modulos.CU5_Reporte.Application.dto;

public class FiltroEmpleadoDTO {

    private String area;
    private String cargo;
    private String tipoContrato;
    private String estado;
    private Long idEmpleado;

    public FiltroEmpleadoDTO() {}

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
}
package com.rrhh.Modulos.CU3_Nomina.Application.dto;

import java.math.BigDecimal;

public class AjustarNominaRequestDTO {

    private BigDecimal sueldoBase;
    private BigDecimal totalHorasTrabajadas;
    private BigDecimal totalHorasExtra;
    private BigDecimal totalHorasNocturnas;
    private Integer totalMinutosTardanza;
    private Boolean tieneHijos;
    private Integer cantidadHijos;
    private Integer cantidadGuardias;
    private String tipoPensionAplicada;

    public BigDecimal getSueldoBase() { return sueldoBase; }
    public void setSueldoBase(BigDecimal sueldoBase) { this.sueldoBase = sueldoBase; }

    public BigDecimal getTotalHorasTrabajadas() { return totalHorasTrabajadas; }
    public void setTotalHorasTrabajadas(BigDecimal totalHorasTrabajadas) { this.totalHorasTrabajadas = totalHorasTrabajadas; }

    public BigDecimal getTotalHorasExtra() { return totalHorasExtra; }
    public void setTotalHorasExtra(BigDecimal totalHorasExtra) { this.totalHorasExtra = totalHorasExtra; }

    public BigDecimal getTotalHorasNocturnas() { return totalHorasNocturnas; }
    public void setTotalHorasNocturnas(BigDecimal totalHorasNocturnas) { this.totalHorasNocturnas = totalHorasNocturnas; }

    public Integer getTotalMinutosTardanza() { return totalMinutosTardanza; }
    public void setTotalMinutosTardanza(Integer totalMinutosTardanza) { this.totalMinutosTardanza = totalMinutosTardanza; }

    public Boolean getTieneHijos() { return tieneHijos; }
    public void setTieneHijos(Boolean tieneHijos) { this.tieneHijos = tieneHijos; }

    public Integer getCantidadHijos() { return cantidadHijos; }
    public void setCantidadHijos(Integer cantidadHijos) { this.cantidadHijos = cantidadHijos; }

    public Integer getCantidadGuardias() { return cantidadGuardias; }
    public void setCantidadGuardias(Integer cantidadGuardias) { this.cantidadGuardias = cantidadGuardias; }

    public String getTipoPensionAplicada() { return tipoPensionAplicada; }
    public void setTipoPensionAplicada(String tipoPensionAplicada) { this.tipoPensionAplicada = tipoPensionAplicada; }
}

package com.rrhh.Modulos.CU3_Nomina.Application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DetalleNominaResponseDTO {

    private Integer idDetalle;
    private LocalDate fecha;
    private BigDecimal horasTrabajadas;
    private Integer minutosTardanza;
    private Integer minutosExtra;
    private BigDecimal montoHoraExtra;
    private BigDecimal descuentoTardanza;
    private String observacion;

    public DetalleNominaResponseDTO() {}

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getHorasTrabajadas() { return horasTrabajadas; }
    public void setHorasTrabajadas(BigDecimal horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }

    public Integer getMinutosTardanza() { return minutosTardanza; }
    public void setMinutosTardanza(Integer minutosTardanza) { this.minutosTardanza = minutosTardanza; }

    public Integer getMinutosExtra() { return minutosExtra; }
    public void setMinutosExtra(Integer minutosExtra) { this.minutosExtra = minutosExtra; }

    public BigDecimal getMontoHoraExtra() { return montoHoraExtra; }
    public void setMontoHoraExtra(BigDecimal montoHoraExtra) { this.montoHoraExtra = montoHoraExtra; }

    public BigDecimal getDescuentoTardanza() { return descuentoTardanza; }
    public void setDescuentoTardanza(BigDecimal descuentoTardanza) { this.descuentoTardanza = descuentoTardanza; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
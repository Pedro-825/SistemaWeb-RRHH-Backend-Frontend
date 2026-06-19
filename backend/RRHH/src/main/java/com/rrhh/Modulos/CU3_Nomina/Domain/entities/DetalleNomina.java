package com.rrhh.Modulos.CU3_Nomina.Domain.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DetalleNomina {

    private Integer idDetalle;
    private LocalDate fecha;
    private BigDecimal horasTrabajadas;
    private Integer minutosTardanza;
    private Integer minutosExtra;
    private BigDecimal montoHoraExtra;
    private BigDecimal descuentoTardanza;
    private String observacion;
    private Long idEmpleado;
    private Integer idNomina;

    public DetalleNomina() {}

    public DetalleNomina(Integer idDetalle, LocalDate fecha, BigDecimal horasTrabajadas,
                         Integer minutosTardanza, Integer minutosExtra,
                         BigDecimal montoHoraExtra, BigDecimal descuentoTardanza,
                         String observacion, Long idEmpleado, Integer idNomina) {
        this.idDetalle = idDetalle;
        this.fecha = fecha;
        this.horasTrabajadas = horasTrabajadas;
        this.minutosTardanza = minutosTardanza;
        this.minutosExtra = minutosExtra;
        this.montoHoraExtra = montoHoraExtra;
        this.descuentoTardanza = descuentoTardanza;
        this.observacion = observacion;
        this.idEmpleado = idEmpleado;
        this.idNomina = idNomina;
    }

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

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Integer getIdNomina() { return idNomina; }
    public void setIdNomina(Integer idNomina) { this.idNomina = idNomina; }
}
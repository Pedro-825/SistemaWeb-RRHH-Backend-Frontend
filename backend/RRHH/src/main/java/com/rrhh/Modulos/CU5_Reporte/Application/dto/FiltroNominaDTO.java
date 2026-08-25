package com.rrhh.Modulos.CU5_Reporte.Application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FiltroNominaDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String area;
    private Long idEmpleado;
    private BigDecimal salarioMin;
    private BigDecimal salarioMax;
    private String estadoPago;
    private BigDecimal horasExtraMin;
    private BigDecimal horasExtraMax;

    public FiltroNominaDTO() {}

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public BigDecimal getSalarioMin() { return salarioMin; }
    public void setSalarioMin(BigDecimal salarioMin) { this.salarioMin = salarioMin; }

    public BigDecimal getSalarioMax() { return salarioMax; }
    public void setSalarioMax(BigDecimal salarioMax) { this.salarioMax = salarioMax; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public BigDecimal getHorasExtraMin() { return horasExtraMin; }
    public void setHorasExtraMin(BigDecimal horasExtraMin) { this.horasExtraMin = horasExtraMin; }

    public BigDecimal getHorasExtraMax() { return horasExtraMax; }
    public void setHorasExtraMax(BigDecimal horasExtraMax) { this.horasExtraMax = horasExtraMax; }
}
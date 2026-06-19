package com.rrhh.Modulos.CU3_Nomina.Domain.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.rrhh.Shared.prototype.Prototype;

import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
public class Nomina implements Prototype<Nomina> {

    private Integer idNomina;
    private String periodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaEmision;

    private BigDecimal sueldoBase;
    private BigDecimal totalHorasTrabajadas;
    private BigDecimal totalHorasExtra;
    private Integer totalMinutosTardanza;

    private BigDecimal bonifFamiliar;
    private BigDecimal bonifTurnoNocturno;
    private BigDecimal bonifGuardia;
    private BigDecimal bonifRiesgo;
    private BigDecimal bonifCargo;

    private BigDecimal descuentoTardanzas;
    private BigDecimal descuentoLey;
    private BigDecimal asignacionFamiliar;

    private BigDecimal sueldoBruto;
    private BigDecimal sueldoNeto;

    private Integer cantidadHijos;
    private Boolean tieneHijos;
    private Integer cantidadGuardias;
    private BigDecimal totalHorasNocturnas;
    private String tipoPensionAplicada;

    private String estadoPago;

    private Integer version;
    private Integer nominaAnteriorId;

    private Long idEmpleado;
    private Long calculadoPor;

    private List<DetalleNomina> detalles;

    public Nomina(Integer idNomina, String periodo, LocalDate fechaInicio, LocalDate fechaFin,
                  LocalDate fechaEmision, BigDecimal sueldoBase, BigDecimal totalHorasTrabajadas,
                  BigDecimal totalHorasExtra, Integer totalMinutosTardanza,
                  BigDecimal bonifFamiliar, BigDecimal bonifTurnoNocturno,
                  BigDecimal bonifGuardia, BigDecimal bonifRiesgo, BigDecimal bonifCargo,
                  BigDecimal descuentoTardanzas, BigDecimal descuentoLey,
                  BigDecimal asignacionFamiliar, BigDecimal sueldoBruto,
                  BigDecimal sueldoNeto,
                  Integer cantidadHijos, Boolean tieneHijos, Integer cantidadGuardias,
                  BigDecimal totalHorasNocturnas, String tipoPensionAplicada,
                  String estadoPago, Integer version,
                  Integer nominaAnteriorId, Long idEmpleado, Long calculadoPor,
                  List<DetalleNomina> detalles) {
        this.idNomina = idNomina;
        this.periodo = periodo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaEmision = fechaEmision;
        this.sueldoBase = sueldoBase;
        this.totalHorasTrabajadas = totalHorasTrabajadas;
        this.totalHorasExtra = totalHorasExtra;
        this.totalMinutosTardanza = totalMinutosTardanza;
        this.bonifFamiliar = bonifFamiliar;
        this.bonifTurnoNocturno = bonifTurnoNocturno;
        this.bonifGuardia = bonifGuardia;
        this.bonifRiesgo = bonifRiesgo;
        this.bonifCargo = bonifCargo;
        this.descuentoTardanzas = descuentoTardanzas;
        this.descuentoLey = descuentoLey;
        this.asignacionFamiliar = asignacionFamiliar;
        this.sueldoBruto = sueldoBruto;
        this.sueldoNeto = sueldoNeto;
        this.cantidadHijos = cantidadHijos;
        this.tieneHijos = tieneHijos;
        this.cantidadGuardias = cantidadGuardias;
        this.totalHorasNocturnas = totalHorasNocturnas;
        this.tipoPensionAplicada = tipoPensionAplicada;
        this.estadoPago = estadoPago;
        this.version = version;
        this.nominaAnteriorId = nominaAnteriorId;
        this.idEmpleado = idEmpleado;
        this.calculadoPor = calculadoPor;
        this.detalles = detalles;
    }

    public Integer getIdNomina() { return idNomina; }
    public void setIdNomina(Integer idNomina) { this.idNomina = idNomina; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public BigDecimal getSueldoBase() { return sueldoBase; }
    public void setSueldoBase(BigDecimal sueldoBase) { this.sueldoBase = sueldoBase; }

    public BigDecimal getTotalHorasTrabajadas() { return totalHorasTrabajadas; }
    public void setTotalHorasTrabajadas(BigDecimal totalHorasTrabajadas) { this.totalHorasTrabajadas = totalHorasTrabajadas; }

    public BigDecimal getTotalHorasExtra() { return totalHorasExtra; }
    public void setTotalHorasExtra(BigDecimal totalHorasExtra) { this.totalHorasExtra = totalHorasExtra; }

    public Integer getTotalMinutosTardanza() { return totalMinutosTardanza; }
    public void setTotalMinutosTardanza(Integer totalMinutosTardanza) { this.totalMinutosTardanza = totalMinutosTardanza; }

    public BigDecimal getBonifFamiliar() { return bonifFamiliar; }
    public void setBonifFamiliar(BigDecimal bonifFamiliar) { this.bonifFamiliar = bonifFamiliar; }

    public BigDecimal getBonifTurnoNocturno() { return bonifTurnoNocturno; }
    public void setBonifTurnoNocturno(BigDecimal bonifTurnoNocturno) { this.bonifTurnoNocturno = bonifTurnoNocturno; }

    public BigDecimal getBonifGuardia() { return bonifGuardia; }
    public void setBonifGuardia(BigDecimal bonifGuardia) { this.bonifGuardia = bonifGuardia; }

    public BigDecimal getBonifRiesgo() { return bonifRiesgo; }
    public void setBonifRiesgo(BigDecimal bonifRiesgo) { this.bonifRiesgo = bonifRiesgo; }

    public BigDecimal getBonifCargo() { return bonifCargo; }
    public void setBonifCargo(BigDecimal bonifCargo) { this.bonifCargo = bonifCargo; }

    public BigDecimal getDescuentoTardanzas() { return descuentoTardanzas; }
    public void setDescuentoTardanzas(BigDecimal descuentoTardanzas) { this.descuentoTardanzas = descuentoTardanzas; }

    public BigDecimal getDescuentoLey() { return descuentoLey; }
    public void setDescuentoLey(BigDecimal descuentoLey) { this.descuentoLey = descuentoLey; }

    public BigDecimal getAsignacionFamiliar() { return asignacionFamiliar; }
    public void setAsignacionFamiliar(BigDecimal asignacionFamiliar) { this.asignacionFamiliar = asignacionFamiliar; }

    public BigDecimal getSueldoBruto() { return sueldoBruto; }
    public void setSueldoBruto(BigDecimal sueldoBruto) { this.sueldoBruto = sueldoBruto; }

    public BigDecimal getSueldoNeto() { return sueldoNeto; }
    public void setSueldoNeto(BigDecimal sueldoNeto) { this.sueldoNeto = sueldoNeto; }

    public Integer getCantidadHijos() { return cantidadHijos; }
    public void setCantidadHijos(Integer cantidadHijos) { this.cantidadHijos = cantidadHijos; }

    public Boolean getTieneHijos() { return tieneHijos; }
    public void setTieneHijos(Boolean tieneHijos) { this.tieneHijos = tieneHijos; }

    public Integer getCantidadGuardias() { return cantidadGuardias; }
    public void setCantidadGuardias(Integer cantidadGuardias) { this.cantidadGuardias = cantidadGuardias; }

    public BigDecimal getTotalHorasNocturnas() { return totalHorasNocturnas; }
    public void setTotalHorasNocturnas(BigDecimal totalHorasNocturnas) { this.totalHorasNocturnas = totalHorasNocturnas; }

    public String getTipoPensionAplicada() { return tipoPensionAplicada; }
    public void setTipoPensionAplicada(String tipoPensionAplicada) { this.tipoPensionAplicada = tipoPensionAplicada; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Integer getNominaAnteriorId() { return nominaAnteriorId; }
    public void setNominaAnteriorId(Integer nominaAnteriorId) { this.nominaAnteriorId = nominaAnteriorId; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Long getCalculadoPor() { return calculadoPor; }
    public void setCalculadoPor(Long calculadoPor) { this.calculadoPor = calculadoPor; }

    public List<DetalleNomina> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleNomina> detalles) { this.detalles = detalles; }

    @Override
    public Nomina clonePrototype() {
        Nomina clon = new Nomina();
        clon.setSueldoBase(this.sueldoBase);
        clon.setEstadoPago("CALCULADA");
        return clon;
    }
}
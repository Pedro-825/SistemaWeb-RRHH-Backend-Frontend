package com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.rrhh.Shared.prototype.Prototype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAsistencia implements Prototype<RegistroAsistencia> {

    private Integer idRegistro;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    @Builder.Default
    private BigDecimal horasTrabajadas = BigDecimal.ZERO;
    @Builder.Default
    private Integer minutosTardanza = 0;
    @Builder.Default
    private Integer minutosExtra = 0;
    @Builder.Default
    private String estado = "PUNTUAL";
    private String tipoUltimoRegistro;
    private String observacion;
    @Builder.Default
    private LocalDateTime creadoEl = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime actualizadoEl = LocalDateTime.now();
    private Empleado empleado;
    private Horario horario;

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public BigDecimal getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(BigDecimal horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    public Integer getMinutosTardanza() {
        return minutosTardanza;
    }

    public void setMinutosTardanza(Integer minutosTardanza) {
        this.minutosTardanza = minutosTardanza;
    }

    public Integer getMinutosExtra() {
        return minutosExtra;
    }

    public void setMinutosExtra(Integer minutosExtra) {
        this.minutosExtra = minutosExtra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoUltimoRegistro() {
        return tipoUltimoRegistro;
    }

    public void setTipoUltimoRegistro(String tipoUltimoRegistro) {
        this.tipoUltimoRegistro = tipoUltimoRegistro;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getCreadoEl() {
        return creadoEl;
    }

    public void setCreadoEl(LocalDateTime creadoEl) {
        this.creadoEl = creadoEl;
    }

    public LocalDateTime getActualizadoEl() {
        return actualizadoEl;
    }

    public void setActualizadoEl(LocalDateTime actualizadoEl) {
        this.actualizadoEl = actualizadoEl;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    @Override
    public RegistroAsistencia clonePrototype() {
        RegistroAsistencia clon = new RegistroAsistencia();
        clon.setEstado("PUNTUAL");
        clon.setMinutosTardanza(0);
        clon.setMinutosExtra(0);
        clon.setHorasTrabajadas(BigDecimal.ZERO);
        return clon;
    }
}

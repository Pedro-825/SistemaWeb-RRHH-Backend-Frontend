package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "registro_asistencia")
public class RegistroAsistenciaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer idRegistro;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "horas_trabajadas")
    private BigDecimal horasTrabajadas = BigDecimal.ZERO;

    @Column(name = "minutos_tardanza")
    private Integer minutosTardanza = 0;

    @Column(name = "minutos_extra")
    private Integer minutosExtra = 0;

    @Column(name = "estado", length = 30)
    private String estado = "PUNTUAL";

    @Column(name = "tipo_ultimo_registro", length = 20)
    private String tipoUltimoRegistro;

    @Column(name = "tipo_registro", length = 20)
    private String tipoRegistro = "ORIGINAL";

    @Column(name = "observacion", length = 255)
    private String observacion;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private EmpleadoModel empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario")
    private HorarioModel horario;

    public RegistroAsistenciaModel() {
    }

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

    public String getTipoRegistro() { return tipoRegistro; }
    public void setTipoRegistro(String tipoRegistro) { this.tipoRegistro = tipoRegistro; }

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

    public EmpleadoModel getEmpleado() {
        return empleado;
    }

    public void setEmpleado(EmpleadoModel empleado) {
        this.empleado = empleado;
    }

    public HorarioModel getHorario() {
        return horario;
    }

    public void setHorario(HorarioModel horario) {
        this.horario = horario;
    }
}
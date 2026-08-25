package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "plan_recuperacion")
public class PlanRecuperacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Integer idPlan;

    @Column(name = "id_justificacion", nullable = false)
    private Integer idJustificacion;

    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida_original", nullable = false)
    private LocalTime horaSalidaOriginal;

    @Column(name = "minutos_recuperacion", nullable = false)
    private Integer minutosRecuperacion;

    @Column(name = "hora_salida_modificada", nullable = false)
    private LocalTime horaSalidaModificada;

    @Column(name = "creado_el", nullable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    public Integer getIdPlan() { return idPlan; }
    public void setIdPlan(Integer idPlan) { this.idPlan = idPlan; }

    public Integer getIdJustificacion() { return idJustificacion; }
    public void setIdJustificacion(Integer idJustificacion) { this.idJustificacion = idJustificacion; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalidaOriginal() { return horaSalidaOriginal; }
    public void setHoraSalidaOriginal(LocalTime horaSalidaOriginal) { this.horaSalidaOriginal = horaSalidaOriginal; }

    public Integer getMinutosRecuperacion() { return minutosRecuperacion; }
    public void setMinutosRecuperacion(Integer minutosRecuperacion) { this.minutosRecuperacion = minutosRecuperacion; }

    public LocalTime getHoraSalidaModificada() { return horaSalidaModificada; }
    public void setHoraSalidaModificada(LocalTime horaSalidaModificada) { this.horaSalidaModificada = horaSalidaModificada; }

    public LocalDateTime getCreadoEl() { return creadoEl; }
    public void setCreadoEl(LocalDateTime creadoEl) { this.creadoEl = creadoEl; }
}

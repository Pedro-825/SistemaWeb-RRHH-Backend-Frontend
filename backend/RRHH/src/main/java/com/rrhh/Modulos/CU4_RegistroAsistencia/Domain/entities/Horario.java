package com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities;

import java.time.LocalTime;

public class Horario {

    private Integer idHorario;
    private String nombreTurno;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private Integer tolerancia = 10;
    private Integer umbralExtra = 60;

    public Horario() {}

    public Integer getIdHorario() { return idHorario; }
    public void setIdHorario(Integer idHorario) { this.idHorario = idHorario; }

    public String getNombreTurno() { return nombreTurno; }
    public void setNombreTurno(String nombreTurno) { this.nombreTurno = nombreTurno; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }

    public Integer getTolerancia() { return tolerancia; }
    public void setTolerancia(Integer tolerancia) { this.tolerancia = tolerancia; }

    public Integer getUmbralExtra() { return umbralExtra; }
    public void setUmbralExtra(Integer umbralExtra) { this.umbralExtra = umbralExtra; }
}

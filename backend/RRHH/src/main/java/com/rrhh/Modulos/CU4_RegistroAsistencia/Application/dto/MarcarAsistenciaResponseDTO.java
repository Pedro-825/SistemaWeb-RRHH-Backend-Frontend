package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class MarcarAsistenciaResponseDTO {

    private boolean success;
    private String message;
    private String tipoRegistro;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private BigDecimal horasTrabajadas;
    private Integer minutosTardanza;
    private Integer minutosExtra;
    private String estado;

    public MarcarAsistenciaResponseDTO() {
    }

    public MarcarAsistenciaResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public MarcarAsistenciaResponseDTO(boolean success, String message, String tipoRegistro,
                                       LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida,
                                       BigDecimal horasTrabajadas, Integer minutosTardanza,
                                       Integer minutosExtra, String estado) {
        this.success = success;
        this.message = message;
        this.tipoRegistro = tipoRegistro;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.horasTrabajadas = horasTrabajadas;
        this.minutosTardanza = minutosTardanza;
        this.minutosExtra = minutosExtra;
        this.estado = estado;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getTipoRegistro() {
        return tipoRegistro;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public BigDecimal getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public Integer getMinutosTardanza() {
        return minutosTardanza;
    }

    public Integer getMinutosExtra() {
        return minutosExtra;
    }

    public String getEstado() {
        return estado;
    }
}
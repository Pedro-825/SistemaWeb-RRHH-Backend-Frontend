package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto;

import java.time.LocalDateTime;

public class MarcarAsistenciaManualRequestDTO {

    private Integer idEmpleado;
    private String tipo;
    private LocalDateTime fechaHora;
    private String motivo;
    private String tipoRegistro = "ORIGINAL";
    private Long idJustificacion;

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getTipoRegistro() { return tipoRegistro; }
    public void setTipoRegistro(String tipoRegistro) { this.tipoRegistro = tipoRegistro; }

    public Long getIdJustificacion() { return idJustificacion; }
    public void setIdJustificacion(Long idJustificacion) { this.idJustificacion = idJustificacion; }
}

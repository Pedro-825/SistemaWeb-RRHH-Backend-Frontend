package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion_envio")
public class NotificacionEnvioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @Column(name = "id_nomina", nullable = false)
    private Integer idNomina;

    @Column(name = "tipo", length = 30)
    private String tipo = "COMPROBANTE_NOMINA";

    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "intentos")
    private Integer intentos = 0;

    @Column(name = "max_intentos")
    private Integer maxIntentos = 3;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "creado_el")
    private LocalDateTime creadoEl = LocalDateTime.now();

    public NotificacionEnvioModel() {}

    public Integer getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Integer idNotificacion) { this.idNotificacion = idNotificacion; }

    public Integer getIdNomina() { return idNomina; }
    public void setIdNomina(Integer idNomina) { this.idNomina = idNomina; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }

    public Integer getMaxIntentos() { return maxIntentos; }
    public void setMaxIntentos(Integer maxIntentos) { this.maxIntentos = maxIntentos; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getCreadoEl() { return creadoEl; }
    public void setCreadoEl(LocalDateTime creadoEl) { this.creadoEl = creadoEl; }
}

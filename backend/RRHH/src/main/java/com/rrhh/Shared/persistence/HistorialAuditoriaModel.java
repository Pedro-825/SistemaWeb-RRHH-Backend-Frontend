package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_auditoria")
public class HistorialAuditoriaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @Column(name = "tabla_afectada", nullable = false)
    private String tablaAfectada;

    @Column(name = "id_registro_afectado", nullable = false)
    private Long idRegistroAfectado;

    @Column(name = "accion", nullable = false, length = 70)
    private String accion;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    @Column(name = "ip_maquina")
    private String ipMaquina;

    @Column(name = "fecha_de_cambio")
    private LocalDateTime fechaDeCambio = LocalDateTime.now();

    @Column(name = "creado_el")
    private LocalDateTime creadoEl = LocalDateTime.now();

    // =========================
    // RELACIONES
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private UsuarioModel usuario;

    // =========================
    // CONSTRUCTORES
    // =========================

    public HistorialAuditoriaModel() {
    }

    public HistorialAuditoriaModel(
            Long idAuditoria,
            String tablaAfectada,
            Long idRegistroAfectado,
            String accion,
            String valorAnterior,
            String valorNuevo,
            String ipMaquina,
            LocalDateTime fechaDeCambio,
            LocalDateTime creadoEl,
            UsuarioModel usuario
    ) {
        this.idAuditoria = idAuditoria;
        this.tablaAfectada = tablaAfectada;
        this.idRegistroAfectado = idRegistroAfectado;
        this.accion = accion;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.ipMaquina = ipMaquina;
        this.fechaDeCambio = fechaDeCambio;
        this.creadoEl = creadoEl;
        this.usuario = usuario;
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public Long getIdRegistroAfectado() {
        return idRegistroAfectado;
    }

    public void setIdRegistroAfectado(Long idRegistroAfectado) {
        this.idRegistroAfectado = idRegistroAfectado;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getIpMaquina() {
        return ipMaquina;
    }

    public void setIpMaquina(String ipMaquina) {
        this.ipMaquina = ipMaquina;
    }

    public LocalDateTime getFechaDeCambio() {
        return fechaDeCambio;
    }

    public void setFechaDeCambio(LocalDateTime fechaDeCambio) {
        this.fechaDeCambio = fechaDeCambio;
    }

    public LocalDateTime getCreadoEl() {
        return creadoEl;
    }

    public void setCreadoEl(LocalDateTime creadoEl) {
        this.creadoEl = creadoEl;
    }

    public UsuarioModel getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioModel usuario) {
        this.usuario = usuario;
    }
}
package com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities;

import com.rrhh.Shared.prototype.Prototype;
import java.time.LocalDateTime;

public class Historial implements Prototype<Historial> {

    private Long idAuditoria;
    private Long idUsuario;
    private Long idRegistroAfectado;
    private String accion;
    private String tablaAfectada;
    private String ipMaquina;
    private LocalDateTime fechaCambio;
    private String valorAnterior;
    private String valorNuevo;

    public Historial() {
    }

    // Constructor usado para login
    public Historial(Long idUsuario, String accion, String ip) {
        this.idUsuario = idUsuario;
        this.idRegistroAfectado = idUsuario;
        this.accion = accion;
        this.ipMaquina = ip;
        this.tablaAfectada = "USUARIO";
        this.fechaCambio = LocalDateTime.now();
    }

    // Constructor más completo para otros módulos
    public Historial(
            Long idUsuario,
            Long idRegistroAfectado,
            String accion,
            String tablaAfectada,
            String ipMaquina,
            String valorAnterior,
            String valorNuevo
    ) {
        this.idUsuario = idUsuario;
        this.idRegistroAfectado = idRegistroAfectado;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.ipMaquina = ipMaquina;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.fechaCambio = LocalDateTime.now();
    }

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public String getIpMaquina() {
        return ipMaquina;
    }

    public void setIpMaquina(String ipMaquina) {
        this.ipMaquina = ipMaquina;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
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

    @Override
    public Historial clonePrototype() {
        Historial copia = new Historial();
        copia.setIdAuditoria(this.idAuditoria);
        copia.setIdUsuario(this.idUsuario);
        copia.setIdRegistroAfectado(this.idRegistroAfectado);
        copia.setAccion(this.accion);
        copia.setTablaAfectada(this.tablaAfectada);
        copia.setIpMaquina(this.ipMaquina);
        copia.setFechaCambio(this.fechaCambio);
        copia.setValorAnterior(this.valorAnterior);
        copia.setValorNuevo(this.valorNuevo);
        return copia;
    }
}
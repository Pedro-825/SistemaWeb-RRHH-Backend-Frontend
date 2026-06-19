package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "departamento")
public class DepartamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dpto")
    private Integer idDpto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cod_dpto", unique = true, length = 20)
    private String codDpto;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    // jefe_dpto es una FK hacia empleado.id_empleado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jefe_dpto", referencedColumnName = "id_empleado")
    private EmpleadoModel jefeDpto;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    // Como quitaste id_dpto de empleado,
    // ahora departamento se relaciona con contrato.
    @OneToMany(mappedBy = "departamento", fetch = FetchType.LAZY)
    private List<ContratoModel> contratos;

    // ================= CONSTRUCTORES =================

    public DepartamentoModel() {
    }

    public DepartamentoModel(
            Integer idDpto,
            String nombre,
            String codDpto,
            String ubicacion,
            EmpleadoModel jefeDpto,
            Boolean activo,
            LocalDateTime creadoEl,
            LocalDateTime actualizadoEl,
            List<ContratoModel> contratos
    ) {
        this.idDpto = idDpto;
        this.nombre = nombre;
        this.codDpto = codDpto;
        this.ubicacion = ubicacion;
        this.jefeDpto = jefeDpto;
        this.activo = activo;
        this.creadoEl = creadoEl;
        this.actualizadoEl = actualizadoEl;
        this.contratos = contratos;
    }

    // ================= GETTERS Y SETTERS =================

    public Integer getIdDpto() {
        return idDpto;
    }

    public void setIdDpto(Integer idDpto) {
        this.idDpto = idDpto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodDpto() {
        return codDpto;
    }

    public void setCodDpto(String codDpto) {
        this.codDpto = codDpto;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public EmpleadoModel getJefeDpto() {
        return jefeDpto;
    }

    public void setJefeDpto(EmpleadoModel jefeDpto) {
        this.jefeDpto = jefeDpto;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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

    public List<ContratoModel> getContratos() {
        return contratos;
    }

    public void setContratos(List<ContratoModel> contratos) {
        this.contratos = contratos;
    }
}
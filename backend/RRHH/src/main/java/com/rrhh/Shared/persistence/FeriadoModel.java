package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "feriado")
public class FeriadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feriado")
    private Integer idFeriado;

    @Column(name = "fecha", nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    public FeriadoModel() {}

    public FeriadoModel(LocalDate fecha, String descripcion) {
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public Integer getIdFeriado() { return idFeriado; }
    public void setIdFeriado(Integer idFeriado) { this.idFeriado = idFeriado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}

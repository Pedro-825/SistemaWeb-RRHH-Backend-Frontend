package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "galeria_foto")
public class GaleriaFotoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();

    public GaleriaFotoModel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}

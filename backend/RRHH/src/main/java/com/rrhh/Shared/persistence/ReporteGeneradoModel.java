package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_generado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGeneradoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;

    @Column(name = "tipo_reporte", nullable = false, length = 50)
    private String tipoReporte;

    @Column(name = "filtros_aplicados", columnDefinition = "TEXT")
    private String filtrosAplicados;

    @Column(name = "total_registros")
    private Integer totalRegistros = 0;

    @Column(name = "formato_exportacion", length = 10)
    private String formatoExportacion;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generado_por")
    private UsuarioModel generadoPor;
}
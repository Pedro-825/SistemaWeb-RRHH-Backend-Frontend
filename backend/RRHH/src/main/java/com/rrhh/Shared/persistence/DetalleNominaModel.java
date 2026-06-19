package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_nomina")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleNominaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "horas_trabajadas")
    private BigDecimal horasTrabajadas = BigDecimal.ZERO;

    @Column(name = "minutos_tardanza")
    private Integer minutosTardanza = 0;

    @Column(name = "minutos_extra")
    private Integer minutosExtra = 0;

    @Column(name = "monto_hora_extra")
    private BigDecimal montoHoraExtra = BigDecimal.ZERO;

    @Column(name = "descuento_tardanza")
    private BigDecimal descuentoTardanza = BigDecimal.ZERO;

    @Column(name = "observacion", length = 255)
    private String observacion;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nomina", nullable = false)
    private NominaModel nomina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private EmpleadoModel empleado;
}
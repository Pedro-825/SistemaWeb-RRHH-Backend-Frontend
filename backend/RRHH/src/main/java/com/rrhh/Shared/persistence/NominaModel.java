package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "nomina")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NominaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nomina")
    private Integer idNomina;

    @Column(name = "periodo", nullable = false, length = 20)
    private String periodo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "sueldo_base")
    private BigDecimal sueldoBase;

    @Column(name = "total_horas_trabajadas")
    private BigDecimal totalHorasTrabajadas = BigDecimal.ZERO;

    @Column(name = "total_horas_extra")
    private BigDecimal totalHorasExtra = BigDecimal.ZERO;

    @Column(name = "total_minutos_tardanza")
    private Integer totalMinutosTardanza = 0;

    // Bonificaciones
    @Column(name = "bonif_familiar")
    private BigDecimal bonifFamiliar = BigDecimal.ZERO;

    @Column(name = "bonif_turno_nocturno")
    private BigDecimal bonifTurnoNocturno = BigDecimal.ZERO;

    @Column(name = "bonif_guardia")
    private BigDecimal bonifGuardia = BigDecimal.ZERO;

    @Column(name = "bonif_riesgo")
    private BigDecimal bonifRiesgo = BigDecimal.ZERO;

    @Column(name = "bonif_cargo")
    private BigDecimal bonifCargo = BigDecimal.ZERO;

    // Descuentos
    @Column(name = "descuento_tardanzas")
    private BigDecimal descuentoTardanzas = BigDecimal.ZERO;

    @Column(name = "descuento_ley")
    private BigDecimal descuentoLey = BigDecimal.ZERO;

    @Column(name = "asignacion_familiar")
    private BigDecimal asignacionFamiliar = BigDecimal.ZERO;

    // Totales
    @Column(name = "sueldo_bruto")
    private BigDecimal sueldoBruto = BigDecimal.ZERO;

    @Column(name = "sueldo_neto", nullable = false)
    private BigDecimal sueldoNeto;

    @Column(name = "cantidad_hijos")
    private Integer cantidadHijos = 0;

    @Column(name = "tiene_hijos")
    private Boolean tieneHijos = false;

    @Column(name = "cantidad_guardias")
    private Integer cantidadGuardias = 0;

    @Column(name = "total_horas_nocturnas")
    private BigDecimal totalHorasNocturnas = BigDecimal.ZERO;

    @Column(name = "tipo_pension_aplicada", length = 10)
    private String tipoPensionAplicada = "ONP";

    @Column(name = "estado_pago", length = 20)
    private String estadoPago = "CALCULADA";

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "nomina_anterior_id")
    private Integer nominaAnteriorId;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private EmpleadoModel empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculado_por")
    private UsuarioModel calculadoPor;

    @OneToMany(mappedBy = "nomina", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleNominaModel> detalles;
}
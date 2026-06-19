package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "familia_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FamiliaInfoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_familia_info")
    private Integer idFamiliaInfo;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "parentesco", nullable = false)
    private String parentesco;

    @Column(name = "numero_di", unique = true)
    private String numeroDi;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_el")
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private EmpleadoModel empleado;
}
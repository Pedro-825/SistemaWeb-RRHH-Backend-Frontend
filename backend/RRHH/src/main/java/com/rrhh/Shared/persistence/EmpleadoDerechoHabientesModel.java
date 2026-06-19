package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "empleado_derecho_habientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDerechoHabientesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emp_dh")
    private Long idEmpDh;

    @Column(name = "regimen_salud")
    private String regimenSalud;

    @Column(name = "regimen_pension")
    private String regimenPension;

    @Column(name = "contacto_emergencia")
    private String contactoEmergencia;

    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_el")
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private EmpleadoModel empleado;
}
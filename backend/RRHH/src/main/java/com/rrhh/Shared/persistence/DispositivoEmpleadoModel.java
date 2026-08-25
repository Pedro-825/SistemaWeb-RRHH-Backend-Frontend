package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispositivo_empleado")
public class DispositivoEmpleadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dispositivo")
    private Long idDispositivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false, unique = true)
    private EmpleadoModel empleado;

    @Column(name = "device_token", nullable = false, length = 255)
    private String deviceToken;

    @Column(name = "registrado_el")
    private LocalDateTime registradoEl = LocalDateTime.now();

    public Long getIdDispositivo() { return idDispositivo; }
    public EmpleadoModel getEmpleado() { return empleado; }
    public void setEmpleado(EmpleadoModel empleado) { this.empleado = empleado; }
    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
    public LocalDateTime getRegistradoEl() { return registradoEl; }
}

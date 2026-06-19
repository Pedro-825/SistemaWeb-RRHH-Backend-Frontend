package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "empleado")
public class EmpleadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long idEmpleado;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "doc_identidad", nullable = false, length = 15)
    private String docIdentidad;

    @Column(name = "numero_di", nullable = false, unique = true, length = 15)
    private String numeroDi;

    @Column(name = "fecha_nac", nullable = false)
    private LocalDate fechaNac;

    @Column(name = "sexo", nullable = false, length = 1)
    private String sexo;

    @Column(name = "estado_civil", length = 20)
    private String estadoCivil = "SOLTERO";

    @Column(name = "direccion", nullable = false, length = 80)
    private String direccion;

    @Column(name = "correo", nullable = false, length = 50)
    private String correo;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "ACTIVO";

    @Column(name = "huella_template", columnDefinition = "TEXT")
    private String huellaTemplate;

    @Column(name = "saldo_vacaciones")
    private Integer saldoVacaciones = 30;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    @Column(name = "fecha_desvinculacion")
    private LocalDateTime fechaDesvinculacion;

    // =========================
    // RELACIONES
    // =========================

    @OneToOne(mappedBy = "empleado", fetch = FetchType.LAZY)
    private UsuarioModel usuario;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY)
    private List<SancionModel> sanciones;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY)
    private List<ContratoModel> contratos;

    // =========================
    // CONSTRUCTORES
    // =========================

    public EmpleadoModel() {
    }

    public EmpleadoModel(
            Long idEmpleado,
            String nombres,
            String apellidos,
            String docIdentidad,
            String numeroDi,
            LocalDate fechaNac,
            String sexo,
            String estadoCivil,
            String direccion,
            String correo,
            String telefono,
            String estado,
            LocalDateTime creadoEl,
            LocalDateTime actualizadoEl,
            UsuarioModel usuario,
            List<ContratoModel> contratos,
            List<SancionModel> sanciones
    ) {
        this.idEmpleado = idEmpleado;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.docIdentidad = docIdentidad;
        this.numeroDi = numeroDi;
        this.fechaNac = fechaNac;
        this.sexo = sexo;
        this.estadoCivil = estadoCivil;
        this.direccion = direccion;
        this.correo = correo;
        this.telefono = telefono;
        this.estado = estado;
        this.creadoEl = creadoEl;
        this.actualizadoEl = actualizadoEl;
        this.usuario = usuario;
        this.contratos = contratos;
        this.sanciones = sanciones;
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDocIdentidad() {
        return docIdentidad;
    }

    public void setDocIdentidad(String docIdentidad) {
        this.docIdentidad = docIdentidad;
    }

    public String getNumeroDi() {
        return numeroDi;
    }

    public void setNumeroDi(String numeroDi) {
        this.numeroDi = numeroDi;
    }

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getHuellaTemplate() {
        return huellaTemplate;
    }

    public void setHuellaTemplate(String huellaTemplate) {
        this.huellaTemplate = huellaTemplate;
    }

    public Integer getSaldoVacaciones() {
        return saldoVacaciones;
    }

    public void setSaldoVacaciones(Integer saldoVacaciones) {
        this.saldoVacaciones = saldoVacaciones;
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

    public LocalDateTime getFechaDesvinculacion() {
        return fechaDesvinculacion;
    }

    public void setFechaDesvinculacion(LocalDateTime fechaDesvinculacion) {
        this.fechaDesvinculacion = fechaDesvinculacion;
    }

    public UsuarioModel getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioModel usuario) {
        this.usuario = usuario;
    }

    public List<ContratoModel> getContratos() {
        return contratos;
    }

    public void setContratos(List<ContratoModel> contratos) {
        this.contratos = contratos;
    }

    public List<SancionModel> getSanciones() {
        return sanciones;
    }

    public void setSanciones(List<SancionModel> sanciones) {
        this.sanciones = sanciones;
    }
}
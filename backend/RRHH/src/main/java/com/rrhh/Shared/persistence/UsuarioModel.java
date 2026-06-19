package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 50)
    private String nombreUsuario;

    @Column(name = "correo_inst", unique = true, nullable = false, length = 50)
    private String correoInst;

    @Column(name = "contrasenia", nullable = false, length = 255)
    private String contrasenia;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos = 0;

    @Column(name = "cuenta_bloqueada")
    private Boolean cuentaBloqueada = false;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    @Column(name = "dosfa_activo")
    private Boolean dosfaActivo = false;

    @Column(name = "dosfa_secret", length = 255)
    private String dosfaSecret;

    @Column(name = "intentos_falla_2fa")
    private Integer intentosFalla2fa = 0;

    @Column(name = "bloqueo_2fa_hasta")
    private LocalDateTime bloqueo2faHasta;

    @Column(name = "token_invalido_desde")
    private LocalDateTime tokenInvalidoDesde;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    // =========================
    // RELACIONES
    // =========================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    private RolModel rol;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", referencedColumnName = "id_empleado")
    private EmpleadoModel empleado;

    // =========================
    // CONSTRUCTORES
    // =========================

    public UsuarioModel() {
    }

    public UsuarioModel(
            Long idUsuario,
            String nombreUsuario,
            String correoInst,
            String contrasenia,
            Integer intentosFallidos,
            Boolean cuentaBloqueada,
            LocalDateTime fechaBloqueo,
            Boolean dosfaActivo,
            String dosfaSecret,
            Boolean activo,
            Integer intentosFalla2fa,
            LocalDateTime bloqueo2faHasta,
            LocalDateTime creadoEl,
            LocalDateTime actualizadoEl,
            RolModel rol,
            EmpleadoModel empleado
    ) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.correoInst = correoInst;
        this.contrasenia = contrasenia;
        this.intentosFallidos = intentosFallidos;
        this.cuentaBloqueada = cuentaBloqueada;
        this.fechaBloqueo = fechaBloqueo;
        this.dosfaActivo = dosfaActivo;
        this.dosfaSecret = dosfaSecret;
        this.activo = activo;
        this.intentosFalla2fa = intentosFalla2fa;
        this.bloqueo2faHasta = bloqueo2faHasta;
        this.creadoEl = creadoEl;
        this.actualizadoEl = actualizadoEl;
        this.rol = rol;
        this.empleado = empleado;
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getCorreoInst() {
        return correoInst;
    }

    public void setCorreoInst(String correoInst) {
        this.correoInst = correoInst;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public Boolean getCuentaBloqueada() {
        return cuentaBloqueada;
    }

    public void setCuentaBloqueada(Boolean cuentaBloqueada) {
        this.cuentaBloqueada = cuentaBloqueada;
    }

    public LocalDateTime getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(LocalDateTime fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }

    public Boolean getDosfaActivo() {
        return dosfaActivo;
    }

    public void setDosfaActivo(Boolean dosfaActivo) {
        this.dosfaActivo = dosfaActivo;
    }

    public String getDosfaSecret() {
        return dosfaSecret;
    }

    public void setDosfaSecret(String dosfaSecret) {
        this.dosfaSecret = dosfaSecret;
    }

    public Integer getIntentosFalla2fa() {
        return intentosFalla2fa;
    }

    public void setIntentosFalla2fa(Integer intentosFalla2fa) {
        this.intentosFalla2fa = intentosFalla2fa;
    }

    public LocalDateTime getBloqueo2faHasta() {
        return bloqueo2faHasta;
    }

    public void setBloqueo2faHasta(LocalDateTime bloqueo2faHasta) {
        this.bloqueo2faHasta = bloqueo2faHasta;
    }

    public LocalDateTime getTokenInvalidoDesde() {
        return tokenInvalidoDesde;
    }

    public void setTokenInvalidoDesde(LocalDateTime tokenInvalidoDesde) {
        this.tokenInvalidoDesde = tokenInvalidoDesde;
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

    public RolModel getRol() {
        return rol;
    }

    public void setRol(RolModel rol) {
        this.rol = rol;
    }

    public EmpleadoModel getEmpleado() {
        return empleado;
    }

    public void setEmpleado(EmpleadoModel empleado) {
        this.empleado = empleado;
    }
}
package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reseteo_contrasenia")
public class PasswordResetTokenModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long idToken;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false)
    private UsuarioModel usuario;

    @Column(name = "expiracion", nullable = false)
    private LocalDateTime expiracion;

    @Column(name = "usado")
    private Boolean usado = false;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    public PasswordResetTokenModel() {
    }

    @PrePersist
    public void prePersist() {
        if (this.creadoEl == null) {
            this.creadoEl = LocalDateTime.now();
        }

        if (this.usado == null) {
            this.usado = false;
        }
    }

    public Long getIdToken() {
        return idToken;
    }

    public void setIdToken(Long idToken) {
        this.idToken = idToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuarioModel getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioModel usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(LocalDateTime expiracion) {
        this.expiracion = expiracion;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public LocalDateTime getCreadoEl() {
        return creadoEl;
    }

    public void setCreadoEl(LocalDateTime creadoEl) {
        this.creadoEl = creadoEl;
    }
}
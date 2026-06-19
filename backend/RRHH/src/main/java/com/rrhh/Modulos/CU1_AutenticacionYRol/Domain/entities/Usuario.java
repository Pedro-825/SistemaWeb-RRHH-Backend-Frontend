package com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities;

import java.time.LocalDateTime;

import com.rrhh.Shared.prototype.Prototype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements Prototype<Usuario> {

    private Long id;
    private String nombreUsuario;
    private String correoInst;
    private String contrasenia;
    private int intentosFallidos;
    private boolean cuentaBloqueada;
    private LocalDateTime fechaBloqueo;
    private int intentosFalla2fa;
    private LocalDateTime bloqueo2faHasta;
    private LocalDateTime tokenInvalidoDesde;
    private boolean dosFAActivado;
    private String secret2FA;
    private String nombreRol;
    @Builder.Default
    private Boolean activo = true;
    private String numeroDi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public boolean isCuentaBloqueada() {
        return cuentaBloqueada;
    }

    public void setCuentaBloqueada(boolean cuentaBloqueada) {
        this.cuentaBloqueada = cuentaBloqueada;
    }

    public LocalDateTime getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(LocalDateTime fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }

    public int getIntentosFalla2fa() {
        return intentosFalla2fa;
    }

    public void setIntentosFalla2fa(int intentosFalla2fa) {
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

    public boolean isDosFAActivado() {
        return dosFAActivado;
    }

    public void setDosFAActivado(boolean dosFAActivado) {
        this.dosFAActivado = dosFAActivado;
    }

    public String getSecret2FA() {
        return secret2FA;
    }

    public void setSecret2FA(String secret2FA) {
        this.secret2FA = secret2FA;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public boolean isActivo() {
        return activo != null && activo;
    }

    public String getNumeroDi() {
        return numeroDi;
    }

    public void setNumeroDi(String numeroDi) {
        this.numeroDi = numeroDi;
    }

    public boolean puedeIntentarLogin() {
        if (!cuentaBloqueada) {
            return true;
        }

        if (fechaBloqueo != null && fechaBloqueo.plusMinutes(5).isBefore(LocalDateTime.now())) {
            resetearIntentos();
            return true;
        }

        return false;
    }

    public void registrarFallo() {
        this.intentosFallidos++;

        if (this.intentosFallidos >= 3) {
            this.cuentaBloqueada = true;
            this.fechaBloqueo = LocalDateTime.now();
        }
    }

    public void resetearIntentos() {
        this.intentosFallidos = 0;
        this.cuentaBloqueada = false;
        this.fechaBloqueo = null;
    }

    public boolean puedeIntentar2FA() {
        if (bloqueo2faHasta == null) {
            return true;
        }

        if (bloqueo2faHasta.isBefore(LocalDateTime.now())) {
            resetearIntentos2FA();
            return true;
        }

        return false;
    }

    public void registrarFallo2FA() {
        this.intentosFalla2fa++;

        if (this.intentosFalla2fa >= 3) {
            this.bloqueo2faHasta = LocalDateTime.now().plusMinutes(5);
        }
    }

    public void resetearIntentos2FA() {
        this.intentosFalla2fa = 0;
        this.bloqueo2faHasta = null;
    }

    @Override
    public Usuario clonePrototype() {
        Usuario clon = new Usuario();
        clon.setNombreUsuario(this.nombreUsuario);
        clon.setContrasenia(this.contrasenia);
        clon.setActivo(this.activo);
        clon.setDosFAActivado(this.dosFAActivado);
        return clon;
    }
}
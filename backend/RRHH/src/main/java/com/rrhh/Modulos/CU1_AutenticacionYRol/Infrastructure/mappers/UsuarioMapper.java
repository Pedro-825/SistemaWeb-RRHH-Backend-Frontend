package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.mappers;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.Shared.persistence.UsuarioModel;

public class UsuarioMapper {

    // =========================
    // MODEL -> DOMAIN
    // =========================
    public static Usuario toDomain(UsuarioModel model) {

        if (model == null) {
            return null;
        }

        Usuario domain = new Usuario();

        domain.setId(model.getIdUsuario());

        domain.setNombreUsuario(
                model.getNombreUsuario()
        );

        domain.setCorreoInst(
                model.getCorreoInst()
        );

        domain.setContrasenia(
                model.getContrasenia()
        );

        domain.setIntentosFallidos(
                model.getIntentosFallidos()
        );

        domain.setCuentaBloqueada(
                model.getCuentaBloqueada()
        );

        domain.setFechaBloqueo(
                model.getFechaBloqueo()
        );

        domain.setDosFAActivado(
                model.getDosfaActivo()
        );

        domain.setSecret2FA(
                model.getDosfaSecret()
        );

        domain.setActivo(
                model.getActivo()
        );

        domain.setIntentosFalla2fa(
                model.getIntentosFalla2fa()
        );

        domain.setBloqueo2faHasta(
                model.getBloqueo2faHasta()
        );

        domain.setTokenInvalidoDesde(
                model.getTokenInvalidoDesde()
        );

        if (model.getEmpleado() != null) {
            domain.setNumeroDi(model.getEmpleado().getNumeroDi());
            domain.setIdEmpleado(model.getEmpleado().getIdEmpleado());
        }
        domain.setAppMovilInstalada(Boolean.TRUE.equals(model.getAppMovilInstalada()));

        // =========================
        // ROL
        // =========================
        if (model.getRol() != null) {
            domain.setNombreRol(
                    model.getRol().getNombreRol()
            );
        }

        return domain;
    }

    // =========================
    // DOMAIN -> MODEL
    // =========================
    public static UsuarioModel toModel(Usuario domain) {

        if (domain == null) {
            return null;
        }

        UsuarioModel model = new UsuarioModel();

        model.setIdUsuario(
                domain.getId()
        );

        model.setNombreUsuario(
                domain.getNombreUsuario()
        );

        model.setCorreoInst(
                domain.getCorreoInst()
        );

        model.setContrasenia(
                domain.getContrasenia()
        );

        model.setIntentosFallidos(
                domain.getIntentosFallidos()
        );

        model.setCuentaBloqueada(
                domain.isCuentaBloqueada()
        );

        model.setFechaBloqueo(
                domain.getFechaBloqueo()
        );

        model.setDosfaActivo(
                domain.isDosFAActivado()
        );

        model.setDosfaSecret(
                domain.getSecret2FA()
        );

        model.setActivo(
                domain.isActivo()
        );

        model.setIntentosFalla2fa(
                domain.getIntentosFalla2fa()
        );

        model.setBloqueo2faHasta(
                domain.getBloqueo2faHasta()
        );

        model.setTokenInvalidoDesde(
                domain.getTokenInvalidoDesde()
        );

        /*
         * IMPORTANTE:
         * No asignamos rol aquí.
         * Antes se asignaba idRol = 1 y eso hacía que todos terminaran como RRHH.
         * El rol se asigna al crear el usuario desde Gestión de Empleados.
         */

        return model;
    }
}
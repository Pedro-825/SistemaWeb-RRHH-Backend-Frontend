package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RolModel;
import com.rrhh.Shared.persistence.UsuarioModel;

public class UsuarioGestionMapper {

    public static UsuarioGestion toDomain(UsuarioModel model) {

        if (model == null) {
            return null;
        }

        UsuarioGestion domain = new UsuarioGestion();

        domain.setIdUsuario(model.getIdUsuario());
        domain.setNombreUsuario(model.getNombreUsuario());
        domain.setCorreoInst(model.getCorreoInst());
        domain.setContrasenia(model.getContrasenia());
        domain.setActivo(model.getActivo());
        domain.setDosfaActivo(model.getDosfaActivo());

        if (model.getEmpleado() != null) {
            domain.setIdEmpleado(model.getEmpleado().getIdEmpleado());
        }

        if (model.getRol() != null) {
            domain.setIdRol(model.getRol().getIdRol());
            domain.setNombreRol(model.getRol().getNombreRol());
        }

        return domain;
    }

    public static UsuarioModel toModel(UsuarioGestion domain) {

        if (domain == null) {
            return null;
        }

        UsuarioModel model = new UsuarioModel();

        model.setIdUsuario(domain.getIdUsuario());
        model.setNombreUsuario(domain.getNombreUsuario());
        model.setCorreoInst(domain.getCorreoInst());
        model.setContrasenia(domain.getContrasenia());
        model.setActivo(domain.getActivo());
        model.setDosfaActivo(domain.getDosfaActivo());

        if (domain.getIdEmpleado() != null) {
            EmpleadoModel empleado = new EmpleadoModel();
            empleado.setIdEmpleado(domain.getIdEmpleado());
            model.setEmpleado(empleado);
        }

        if (domain.getIdRol() != null) {
            RolModel rol = new RolModel();
            rol.setIdRol(domain.getIdRol());
            model.setRol(rol);
        }

        return model;
    }
}
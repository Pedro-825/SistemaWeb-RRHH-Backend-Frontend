package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol;
import com.rrhh.Shared.persistence.RolModel;

public class RolGestionMapper {

    public static Rol toDomain(RolModel model) {

        if (model == null) {
            return null;
        }

        Rol domain = new Rol();

        domain.setIdRol(model.getIdRol());
        domain.setNombreRol(model.getNombreRol());
        domain.setActivo(model.getActivo());

        return domain;
    }
}
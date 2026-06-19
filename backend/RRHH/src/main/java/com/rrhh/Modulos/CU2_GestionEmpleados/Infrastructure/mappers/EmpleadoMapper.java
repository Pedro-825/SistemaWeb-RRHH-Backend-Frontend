package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Shared.persistence.EmpleadoModel;

public class EmpleadoMapper {

    public static Empleado toDomain(EmpleadoModel model) {

        if (model == null) {
            return null;
        }

        Empleado domain = new Empleado();

        domain.setIdEmpleado(model.getIdEmpleado());
        domain.setNombres(model.getNombres());
        domain.setApellidos(model.getApellidos());
        domain.setDocIdentidad(model.getDocIdentidad());
        domain.setNumeroDi(model.getNumeroDi());
        domain.setFechaNac(model.getFechaNac());
        domain.setSexo(model.getSexo());
        domain.setEstadoCivil(model.getEstadoCivil());
        domain.setDireccion(model.getDireccion());
        domain.setCorreo(model.getCorreo());
        domain.setTelefono(model.getTelefono());
        domain.setEstado(model.getEstado());
        domain.setFechaDesvinculacion(model.getFechaDesvinculacion());

        return domain;
    }

    public static EmpleadoModel toModel(Empleado domain) {

        if (domain == null) {
            return null;
        }

        EmpleadoModel model = new EmpleadoModel();

        model.setIdEmpleado(domain.getIdEmpleado());
        model.setNombres(domain.getNombres());
        model.setApellidos(domain.getApellidos());
        model.setDocIdentidad(domain.getDocIdentidad());
        model.setNumeroDi(domain.getNumeroDi());
        model.setFechaNac(domain.getFechaNac());
        model.setSexo(domain.getSexo());
        model.setEstadoCivil(domain.getEstadoCivil());
        model.setDireccion(domain.getDireccion());
        model.setCorreo(domain.getCorreo());
        model.setTelefono(domain.getTelefono());
        model.setEstado(domain.getEstado());
        model.setFechaDesvinculacion(domain.getFechaDesvinculacion());

        return model;
    }
}
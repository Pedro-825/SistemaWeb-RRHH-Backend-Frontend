package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.SancionModel;

public class SancionMapper {

    public static Sancion toDomain(SancionModel model) {

        if (model == null) {
            return null;
        }

        Sancion domain = new Sancion();

        domain.setIdSancion(model.getIdSancion());
        domain.setCodigo(model.getCodigo());
        domain.setMotivo(model.getMotivo());
        domain.setDescripcion(model.getDescripcion());
        domain.setFechaInicio(model.getFechaInicio());
        domain.setFechaFin(model.getFechaFin());
        domain.setBloqueaAcceso(model.getBloqueaAcceso());
        domain.setEstado(model.getEstado());

        if (model.getEmpleado() != null) {
            domain.setIdEmpleado(model.getEmpleado().getIdEmpleado());
        }

        return domain;
    }

    public static SancionModel toModel(Sancion domain) {

        if (domain == null) {
            return null;
        }

        SancionModel model = new SancionModel();

        model.setIdSancion(domain.getIdSancion());
        model.setCodigo(domain.getCodigo());
        model.setMotivo(domain.getMotivo());
        model.setDescripcion(domain.getDescripcion());
        model.setFechaInicio(domain.getFechaInicio());
        model.setFechaFin(domain.getFechaFin());
        model.setBloqueaAcceso(domain.getBloqueaAcceso());
        model.setEstado(domain.getEstado());

        if (domain.getIdEmpleado() != null) {
            EmpleadoModel empleado = new EmpleadoModel();
            empleado.setIdEmpleado(domain.getIdEmpleado());
            model.setEmpleado(empleado);
        }

        return model;
    }
}
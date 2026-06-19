package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.DepartamentoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;

public class ContratoMapper {

    public static Contrato toDomain(ContratoModel model) {

        if (model == null) {
            return null;
        }

        Contrato domain = new Contrato();

        domain.setIdContrato(model.getIdContrato());
        domain.setCargo(model.getCargo());
        domain.setTipoContrato(model.getTipoContrato());
        domain.setFechaInicio(model.getFechaInicio());
        domain.setFechaFin(model.getFechaFin());
        domain.setSueldo(model.getSueldo());
        domain.setEstado(model.getEstado());

        if (model.getEmpleado() != null) {
            domain.setIdEmpleado(model.getEmpleado().getIdEmpleado());
        }

        if (model.getDepartamento() != null) {
            domain.setIdDpto(model.getDepartamento().getIdDpto());
            domain.setNombreDepartamento(model.getDepartamento().getNombre());
        }

        return domain;
    }

    public static ContratoModel toModel(Contrato domain) {

        if (domain == null) {
            return null;
        }

        ContratoModel model = new ContratoModel();

        model.setIdContrato(domain.getIdContrato());
        model.setCargo(domain.getCargo());
        model.setTipoContrato(domain.getTipoContrato());
        model.setFechaInicio(domain.getFechaInicio());
        model.setFechaFin(domain.getFechaFin());
        model.setSueldo(domain.getSueldo());
        model.setEstado(domain.getEstado());

        if (domain.getIdEmpleado() != null) {
            EmpleadoModel empleado = new EmpleadoModel();
            empleado.setIdEmpleado(domain.getIdEmpleado());
            model.setEmpleado(empleado);
        }

        if (domain.getIdDpto() != null) {
            DepartamentoModel departamento = new DepartamentoModel();
            departamento.setIdDpto(domain.getIdDpto());
            model.setDepartamento(departamento);
        }

        return model;
    }
}
package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;

public interface IContratoRepository {

    Contrato save(Contrato contrato);

    Contrato findContratoActivoByEmpleado(Long idEmpleado);
}
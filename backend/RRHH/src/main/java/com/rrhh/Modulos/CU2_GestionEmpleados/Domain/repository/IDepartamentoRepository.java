package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;

import java.util.List;

public interface IDepartamentoRepository {

    Departamento findById(Integer idDpto);

    List<Departamento> findAll();
}
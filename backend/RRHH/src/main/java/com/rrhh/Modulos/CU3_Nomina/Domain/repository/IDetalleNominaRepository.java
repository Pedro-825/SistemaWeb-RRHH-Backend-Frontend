package com.rrhh.Modulos.CU3_Nomina.Domain.repository;

import com.rrhh.Modulos.CU3_Nomina.Domain.entities.DetalleNomina;

import java.util.List;

public interface IDetalleNominaRepository {

    DetalleNomina guardar(DetalleNomina detalle);

    List<DetalleNomina> guardarTodos(List<DetalleNomina> detalles);

    List<DetalleNomina> buscarPorNomina(Integer idNomina);

    List<DetalleNomina> buscarPorEmpleado(Long idEmpleado);

    void eliminarPorNomina(Integer idNomina);
}
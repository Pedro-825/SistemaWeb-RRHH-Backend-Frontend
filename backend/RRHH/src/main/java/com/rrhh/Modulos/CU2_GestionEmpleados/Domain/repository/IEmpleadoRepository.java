package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEmpleadoRepository {

    Empleado save(Empleado empleado);

    Empleado findById(Long idEmpleado);

    boolean existsByNumeroDi(String numeroDi);

    List<Empleado> findAll();

    Page<Empleado> findAll(Pageable pageable);

    List<Empleado> buscarPorFiltro(String filtro);

    Page<Empleado> buscarPorFiltro(String filtro, Pageable pageable);

    List<Empleado> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo
    );

    Page<Empleado> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo,
            Pageable pageable
    );
}
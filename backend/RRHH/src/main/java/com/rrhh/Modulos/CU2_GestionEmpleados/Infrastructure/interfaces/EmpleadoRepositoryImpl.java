package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.EmpleadoMapper;
import com.rrhh.Shared.persistence.EmpleadoModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class EmpleadoRepositoryImpl implements IEmpleadoRepository {

    private final JpaEmpleadoRepository jpaRepository;

    public EmpleadoRepositoryImpl(JpaEmpleadoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Empleado save(Empleado empleado) {

        EmpleadoModel model =
                EmpleadoMapper.toModel(empleado);

        EmpleadoModel saved =
                jpaRepository.save(Objects.requireNonNull(model));

        return EmpleadoMapper.toDomain(saved);
    }

    @Override
    public Empleado findById(Long idEmpleado) {

        return jpaRepository.findById(Objects.requireNonNull(idEmpleado))
                .map(EmpleadoMapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByNumeroDi(String numeroDi) {
        return jpaRepository.existsByNumeroDi(numeroDi);
    }

    @Override
    public List<Empleado> findAll() {

        List<EmpleadoModel> models =
                jpaRepository.findAll();

        List<Empleado> empleados =
                new ArrayList<>();

        for (EmpleadoModel model : models) {
            empleados.add(
                    EmpleadoMapper.toDomain(model)
            );
        }

        return empleados;
    }

    @Override
    public Page<Empleado> findAll(Pageable pageable) {
        Page<EmpleadoModel> page = jpaRepository.findAll(Objects.requireNonNull(pageable));
        return page.map(EmpleadoMapper::toDomain);
    }

    @Override
    public List<Empleado> buscarPorFiltro(String filtro) {

        List<EmpleadoModel> models =
                jpaRepository
                        .findByNumeroDiContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                                filtro,
                                filtro,
                                filtro
                        );

        List<Empleado> empleados =
                new ArrayList<>();

        for (EmpleadoModel model : models) {
            empleados.add(
                    EmpleadoMapper.toDomain(model)
            );
        }

        return empleados;
    }

    @Override
    public Page<Empleado> buscarPorFiltro(String filtro, Pageable pageable) {
        Page<EmpleadoModel> page =
                jpaRepository
                        .findByNumeroDiContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                                filtro,
                                filtro,
                                filtro,
                                pageable
                        );

        return page.map(EmpleadoMapper::toDomain);
    }

    @Override
    public List<Empleado> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo
    ) {

        List<EmpleadoModel> models =
                jpaRepository.buscarAvanzado(
                        estado,
                        idDpto,
                        cargo
                );

        List<Empleado> empleados =
                new ArrayList<>();

        for (EmpleadoModel model : models) {
            empleados.add(
                    EmpleadoMapper.toDomain(model)
            );
        }

        return empleados;
    }

    @Override
    public Page<Empleado> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo,
            Pageable pageable
    ) {
        Page<EmpleadoModel> page =
                jpaRepository.buscarAvanzadoPaginado(
                        estado,
                        idDpto,
                        cargo,
                        pageable
                );

        return page.map(EmpleadoMapper::toDomain);
    }
}

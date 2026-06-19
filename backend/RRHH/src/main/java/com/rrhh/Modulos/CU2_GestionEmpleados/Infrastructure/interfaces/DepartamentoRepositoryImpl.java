package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.DepartamentoMapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DepartamentoRepositoryImpl implements IDepartamentoRepository {

    private final JpaDepartamentoRepository jpaRepository;

    public DepartamentoRepositoryImpl(JpaDepartamentoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Departamento findById(Integer idDpto) {
        return jpaRepository.findById(Objects.requireNonNull(idDpto))
                .map(DepartamentoMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Departamento> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(DepartamentoMapper::toDomain)
                .collect(Collectors.toList());
    }
}
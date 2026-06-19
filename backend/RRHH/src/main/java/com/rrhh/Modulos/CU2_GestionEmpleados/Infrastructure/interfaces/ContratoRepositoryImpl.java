package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.ContratoMapper;
import com.rrhh.Shared.persistence.ContratoModel;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ContratoRepositoryImpl implements IContratoRepository {

    private final JpaContratoRepository jpaRepository;

    public ContratoRepositoryImpl(JpaContratoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Contrato save(Contrato contrato) {

        ContratoModel model =
                ContratoMapper.toModel(contrato);

        ContratoModel saved =
                jpaRepository.save(Objects.requireNonNull(model));

        return ContratoMapper.toDomain(saved);
    }

    @Override
    public Contrato findContratoActivoByEmpleado(Long idEmpleado) {

        return jpaRepository
                .findByEmpleadoIdEmpleadoAndEstado(
                        idEmpleado,
                        "ACTIVO"
                )
                .map(ContratoMapper::toDomain)
                .orElse(null);
    }
}
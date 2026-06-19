package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.SancionMapper;
import com.rrhh.Shared.persistence.SancionModel;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class SancionRepositoryImpl implements ISancionRepository {

    private final JpaSancionRepository jpaRepository;

    public SancionRepositoryImpl(JpaSancionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Sancion save(Sancion sancion) {

        SancionModel model =
                SancionMapper.toModel(sancion);

        SancionModel saved =
                jpaRepository.save(Objects.requireNonNull(model));

        return SancionMapper.toDomain(saved);
    }

    @Override
    public List<Sancion> findActivasConBloqueoByEmpleado(Long idEmpleado) {

        List<SancionModel> models =
                jpaRepository.findByEmpleadoIdEmpleadoAndEstadoAndBloqueaAcceso(
                        idEmpleado,
                        "ACTIVA",
                        true
                );

        List<Sancion> sanciones =
                new ArrayList<>();

        for (SancionModel model : models) {
            sanciones.add(
                    SancionMapper.toDomain(model)
            );
        }

        return sanciones;
    }

    @Override
    public List<Sancion> findActivasVencidasConBloqueo(LocalDate fechaActual) {

        List<SancionModel> models =
                jpaRepository.findByEstadoAndBloqueaAccesoAndFechaFinLessThanEqual(
                        "ACTIVA",
                        true,
                        fechaActual
                );

        List<Sancion> sanciones =
                new ArrayList<>();

        for (SancionModel model : models) {
            sanciones.add(
                    SancionMapper.toDomain(model)
            );
        }

        return sanciones;
    }
}
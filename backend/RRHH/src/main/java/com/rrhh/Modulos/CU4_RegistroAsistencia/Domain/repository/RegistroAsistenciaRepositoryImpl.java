package com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.repository;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.RegistroAsistencia;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.factory.IRegistroAsistenciaMapper;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Repository
public class RegistroAsistenciaRepositoryImpl implements IRegistroAsistenciaRepository {

    private final JpaRegistroAsistenciaNominaRepository jpaRegistroAsistenciaRepository;
    private final IRegistroAsistenciaMapper mapper;

    public RegistroAsistenciaRepositoryImpl(
            JpaRegistroAsistenciaNominaRepository jpaRegistroAsistenciaRepository,
            IRegistroAsistenciaMapper mapper) {
        this.jpaRegistroAsistenciaRepository = jpaRegistroAsistenciaRepository;
        this.mapper = mapper;
    }

    @Override
    public RegistroAsistencia guardar(RegistroAsistencia registroAsistencia) {
        RegistroAsistenciaModel model = mapper.crearModeloDesdeDominio(registroAsistencia);
        RegistroAsistenciaModel guardado = jpaRegistroAsistenciaRepository.save(Objects.requireNonNull(model));
        return mapper.crearDominioDesdeModelo(guardado);
    }

    @Override
    public Optional<RegistroAsistencia> buscarPorEmpleadoYFecha(Integer idEmpleado, LocalDate fecha) {
        return jpaRegistroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, fecha, "ORIGINAL")
                .map(mapper::crearDominioDesdeModelo);
    }

    @Override
    public Optional<RegistroAsistencia> buscarPorId(Integer idRegistro) {
        return jpaRegistroAsistenciaRepository.findById(Objects.requireNonNull(idRegistro))
                .map(mapper::crearDominioDesdeModelo);
    }
}

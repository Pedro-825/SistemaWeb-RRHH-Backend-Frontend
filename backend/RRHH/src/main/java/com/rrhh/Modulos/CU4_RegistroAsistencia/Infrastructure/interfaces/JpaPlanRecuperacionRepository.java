package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.PlanRecuperacionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPlanRecuperacionRepository extends JpaRepository<PlanRecuperacionModel, Integer> {

    List<PlanRecuperacionModel> findByIdJustificacionOrderByFechaAsc(Integer idJustificacion);

    boolean existsByIdJustificacion(Integer idJustificacion);
}

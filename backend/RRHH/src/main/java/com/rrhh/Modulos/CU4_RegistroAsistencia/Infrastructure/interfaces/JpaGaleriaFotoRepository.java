package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.GaleriaFotoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaGaleriaFotoRepository extends JpaRepository<GaleriaFotoModel, Long> {
    List<GaleriaFotoModel> findByIdEmpleadoOrderByFechaSubidaDesc(Long idEmpleado);
}

package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.HistorialAuditoriaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface JpaHistorialRepository extends JpaRepository<HistorialAuditoriaModel, Long> {
    List<HistorialAuditoriaModel> findByIdRegistroAfectadoAndTablaAfectada(
        Long idRegistroAfectado, String tablaAfectada, Sort sort);
}

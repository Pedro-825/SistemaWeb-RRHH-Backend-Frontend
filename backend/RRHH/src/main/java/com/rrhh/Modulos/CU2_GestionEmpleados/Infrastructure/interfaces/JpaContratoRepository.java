package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.ContratoModel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaContratoRepository extends JpaRepository<ContratoModel, Long> {

    Optional<ContratoModel> findByEmpleadoIdEmpleadoAndEstado(
            Long idEmpleado,
            String estado
    );

     Optional<ContratoModel> findFirstByEmpleadoIdEmpleadoAndEstado(Long idEmpleado, String estado);
}
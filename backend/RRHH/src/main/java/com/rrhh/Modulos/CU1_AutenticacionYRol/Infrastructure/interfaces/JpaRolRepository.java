package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.RolModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaRolRepository extends JpaRepository<RolModel, Integer> {

    Optional<RolModel> findByNombreRol(String nombreRol);
}
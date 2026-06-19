package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.DepartamentoModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDepartamentoRepository extends JpaRepository<DepartamentoModel, Integer> {
}
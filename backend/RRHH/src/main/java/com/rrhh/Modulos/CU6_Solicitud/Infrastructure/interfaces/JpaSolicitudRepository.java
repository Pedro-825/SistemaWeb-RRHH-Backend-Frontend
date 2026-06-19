package com.rrhh.Modulos.CU6_Solicitud.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.SolicitudModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSolicitudRepository extends JpaRepository<SolicitudModel, Integer> {

    List<SolicitudModel> findByEmpleadoIdEmpleado(Long idEmpleado);

    List<SolicitudModel> findByEstado(String estado);
}
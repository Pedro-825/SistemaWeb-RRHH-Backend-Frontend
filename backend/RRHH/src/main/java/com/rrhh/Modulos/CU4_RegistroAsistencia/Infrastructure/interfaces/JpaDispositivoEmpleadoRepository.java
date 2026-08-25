package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.DispositivoEmpleadoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaDispositivoEmpleadoRepository extends JpaRepository<DispositivoEmpleadoModel, Long> {

    Optional<DispositivoEmpleadoModel> findByEmpleadoIdEmpleado(Long idEmpleado);

    boolean existsByEmpleadoIdEmpleadoAndDeviceToken(Long idEmpleado, String deviceToken);
}

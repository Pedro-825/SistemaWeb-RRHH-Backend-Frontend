package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;
 
import com.rrhh.Shared.persistence.NominaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaNominaRepository extends JpaRepository<NominaModel, Integer> {

    List<NominaModel> findByEmpleadoIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);

    List<NominaModel> findByPeriodo(String periodo);

    List<NominaModel> findByEmpleadoIdEmpleado(Long idEmpleado);

    boolean existsByEmpleadoIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);

    @Modifying
    @Query("DELETE FROM DetalleNominaModel d WHERE d.nomina.periodo = :periodo")
    void deleteDetallesByPeriodo(@Param("periodo") String periodo);

    @Modifying
    @Query("DELETE FROM NotificacionEnvioModel n WHERE n.idNomina = :idNomina")
    void deleteNotificacionesByNominaId(@Param("idNomina") Integer idNomina);

    @Modifying
    @Query("DELETE FROM NotificacionEnvioModel n WHERE n.idNomina IN " +
           "(SELECT nom.idNomina FROM NominaModel nom WHERE nom.periodo = :periodo)")
    void deleteNotificacionesByPeriodo(@Param("periodo") String periodo);

    @Modifying
    @Query("DELETE FROM NominaModel n WHERE n.periodo = :periodo")
    void deleteByPeriodo(@Param("periodo") String periodo);
}
 

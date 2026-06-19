package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;
 
import com.rrhh.Shared.persistence.NominaModel;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface JpaNominaRepository extends JpaRepository<NominaModel, Integer> {
 
    List<NominaModel> findByEmpleadoIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);
 
    List<NominaModel> findByPeriodo(String periodo);
 
    List<NominaModel> findByEmpleadoIdEmpleado(Long idEmpleado);
 
    boolean existsByEmpleadoIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);
}
 
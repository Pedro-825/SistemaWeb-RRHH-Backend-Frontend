
package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;
 
import com.rrhh.Shared.persistence.DetalleNominaModel;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface JpaDetalleNominaRepository extends JpaRepository<DetalleNominaModel, Integer> {
 
    List<DetalleNominaModel> findByNominaIdNomina(Integer idNomina);

    List<DetalleNominaModel> findByEmpleadoIdEmpleado(Long idEmpleado);

    void deleteByNominaIdNomina(Integer idNomina);
}
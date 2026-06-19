package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;
 
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface JpaFamiliaInfoRepository extends JpaRepository<FamiliaInfoModel, Integer> {
 
    List<FamiliaInfoModel> findByEmpleadoIdEmpleadoAndActivoTrue(Long idEmpleado);
}
 
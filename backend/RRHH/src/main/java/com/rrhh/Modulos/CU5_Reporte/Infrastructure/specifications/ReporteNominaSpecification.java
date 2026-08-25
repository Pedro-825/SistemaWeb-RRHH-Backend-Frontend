package com.rrhh.Modulos.CU5_Reporte.Infrastructure.specifications;

import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.NominaModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteNominaSpecification {

    public static Specification<NominaModel> conFiltros(
            Long idEmpleado, String area,
            LocalDate fechaInicio, LocalDate fechaFin,
            BigDecimal salarioMin, BigDecimal salarioMax,
            String estadoPago, BigDecimal horasExtraMin, BigDecimal horasExtraMax) {

        return (root, query, cb) -> {
            query.distinct(true);

            // LEFT JOIN a proposito: una nomina historica de un empleado ya
            // desvinculado/suspendido debe seguir apareciendo en el reporte.
            Join<NominaModel, EmpleadoModel> emp = root.join("empleado");
            Join<EmpleadoModel, ContratoModel> ctr = emp.join("contratos", JoinType.LEFT);
            ctr.on(cb.equal(ctr.get("estado"), "ACTIVO"));

            List<Predicate> predicates = new ArrayList<>();

            if (idEmpleado != null) {
                predicates.add(cb.equal(root.get("empleado").get("idEmpleado"), idEmpleado));
            }
            if (area != null) {
                predicates.add(cb.equal(ctr.get("departamento").get("nombre"), area));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaInicio"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaFin"), fechaFin));
            }
            if (salarioMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sueldoNeto"), salarioMin));
            }
            if (salarioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sueldoNeto"), salarioMax));
            }
            if (estadoPago != null) {
                predicates.add(cb.equal(root.get("estadoPago"), estadoPago));
            }
            if (horasExtraMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalHorasExtra"), horasExtraMin));
            }
            if (horasExtraMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalHorasExtra"), horasExtraMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

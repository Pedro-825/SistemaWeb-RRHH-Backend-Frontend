package com.rrhh.Modulos.CU5_Reporte.Infrastructure.specifications;

import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteAsistenciaSpecification {

    public static Specification<RegistroAsistenciaModel> conFiltros(
            Long idEmpleado, String area,
            LocalDate fechaInicio, LocalDate fechaFin,
            String estado, String tipoRegistro, Integer minutosTardanzaMin) {

        return (root, query, cb) -> {
            query.distinct(true);

            // LEFT JOIN a proposito: si el contrato ya no esta activo (empleado
            // desvinculado/suspendido) el registro de asistencia historico debe
            // seguir apareciendo en el reporte. La condicion ACTIVO va en el ON
            // del join (no en el WHERE) para que no descarte la fila completa.
            Join<RegistroAsistenciaModel, EmpleadoModel> emp = root.join("empleado");
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
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaFin));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (tipoRegistro != null) {
                predicates.add(cb.equal(root.get("tipoRegistro"), tipoRegistro));
            }
            if (minutosTardanzaMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minutosTardanza"), minutosTardanzaMin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

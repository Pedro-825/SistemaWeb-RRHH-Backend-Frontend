package com.rrhh.Modulos.CU5_Reporte.Infrastructure.specifications;

import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.DepartamentoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RolModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReporteEmpleadoSpecification {

    private ReporteEmpleadoSpecification() {
    }

    public static Specification<EmpleadoModel> conFiltros(
            String estado,
            String area,
            String cargo,
            String tipoContrato,
            Long idEmpleado,
            LocalDateTime fechaIngresoDesde,
            LocalDateTime fechaIngresoHasta,
            String rol
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null) {
                query.distinct(true);
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (idEmpleado != null) {
                predicates.add(cb.equal(root.get("idEmpleado"), idEmpleado));
            }

            if (fechaIngresoDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("creadoEl"), fechaIngresoDesde));
            }

            if (fechaIngresoHasta != null) {
                predicates.add(cb.lessThan(root.get("creadoEl"), fechaIngresoHasta));
            }

            Join<EmpleadoModel, ContratoModel> contrato = null;
            if (area != null || cargo != null || tipoContrato != null) {
                contrato = root.join("contratos", JoinType.LEFT);
                contrato.on(cb.equal(contrato.get("estado"), "ACTIVO"));
            }

            if (area != null && contrato != null) {
                Join<ContratoModel, DepartamentoModel> departamento = contrato.join("departamento", JoinType.LEFT);
                predicates.add(cb.equal(departamento.get("nombre"), area));
            }

            if (cargo != null && contrato != null) {
                predicates.add(cb.equal(contrato.get("cargo"), cargo));
            }

            if (tipoContrato != null && contrato != null) {
                predicates.add(cb.equal(contrato.get("tipoContrato"), tipoContrato));
            }

            if (rol != null) {
                Join<EmpleadoModel, UsuarioModel> usuario = root.join("usuario", JoinType.LEFT);
                Join<UsuarioModel, RolModel> rolJoin = usuario.join("rol", JoinType.LEFT);
                predicates.add(cb.equal(rolJoin.get("nombreRol"), rol));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaEmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    boolean existsByNumeroDi(String numeroDi);

    boolean existsByCorreoIgnoreCase(String correo);

    java.util.Optional<EmpleadoModel> findByNumeroDi(String numeroDi);

    List<EmpleadoModel> findByEstado(String estado);

    List<EmpleadoModel> findByNumeroDiContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String numeroDi,
            String nombres,
            String apellidos
    );

    Page<EmpleadoModel> findByNumeroDiContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String numeroDi,
            String nombres,
            String apellidos,
            Pageable pageable
    );

    @Query(value = """
            SELECT e.*
            FROM empleado e
            LEFT JOIN LATERAL (
                SELECT c2.id_dpto, c2.cargo
                FROM contrato c2
                WHERE c2.id_empleado = e.id_empleado
                ORDER BY c2.id_contrato DESC
                LIMIT 1
            ) c ON true
            LEFT JOIN departamento d ON d.id_dpto = c.id_dpto
            WHERE (:estado IS NULL OR e.estado = CAST(:estado AS TEXT))
              AND (:idDpto IS NULL OR d.id_dpto = :idDpto)
              AND (
                    :cargo IS NULL
                    OR LOWER(c.cargo) LIKE LOWER(CONCAT('%', CAST(:cargo AS TEXT), '%'))
                  )
            """, nativeQuery = true)
    List<EmpleadoModel> buscarAvanzado(
            @Param("estado") String estado,
            @Param("idDpto") Integer idDpto,
            @Param("cargo") String cargo
    );

    @Query(value = """
            SELECT e.*
            FROM empleado e
            LEFT JOIN LATERAL (
                SELECT c2.id_dpto, c2.cargo
                FROM contrato c2
                WHERE c2.id_empleado = e.id_empleado
                ORDER BY c2.id_contrato DESC
                LIMIT 1
            ) c ON true
            LEFT JOIN departamento d ON d.id_dpto = c.id_dpto
            WHERE (:estado IS NULL OR e.estado = CAST(:estado AS TEXT))
              AND (:idDpto IS NULL OR d.id_dpto = :idDpto)
              AND (
                    :cargo IS NULL
                    OR LOWER(c.cargo) LIKE LOWER(CONCAT('%', CAST(:cargo AS TEXT), '%'))
                  )
            """,
            countQuery = """
            SELECT COUNT(e.id_empleado)
            FROM empleado e
            LEFT JOIN LATERAL (
                SELECT c2.id_dpto, c2.cargo
                FROM contrato c2
                WHERE c2.id_empleado = e.id_empleado
                ORDER BY c2.id_contrato DESC
                LIMIT 1
            ) c ON true
            LEFT JOIN departamento d ON d.id_dpto = c.id_dpto
            WHERE (:estado IS NULL OR e.estado = CAST(:estado AS TEXT))
              AND (:idDpto IS NULL OR d.id_dpto = :idDpto)
              AND (
                    :cargo IS NULL
                    OR LOWER(c.cargo) LIKE LOWER(CONCAT('%', CAST(:cargo AS TEXT), '%'))
                  )
            """,
            nativeQuery = true)
    Page<EmpleadoModel> buscarAvanzadoPaginado(
            @Param("estado") String estado,
            @Param("idDpto") Integer idDpto,
            @Param("cargo") String cargo,
            Pageable pageable
    );
}
package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface JpaUsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByNombreUsuario(String nombreUsuario);

    @Query("""
        SELECT u FROM UsuarioModel u
        LEFT JOIN FETCH u.empleado
        WHERE u.nombreUsuario = :nombreUsuario OR u.correoInst = :correoInst
        """)
    Optional<UsuarioModel> findByNombreUsuarioOrCorreoInst(
        @Param("nombreUsuario") String nombreUsuario,
        @Param("correoInst") String correoInst
    );
    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByCorreoInst(String correoInst);

    Optional<UsuarioModel> findByEmpleadoIdEmpleado(Long idEmpleado);
    Optional<UsuarioModel> findByCorreoInst(String correoInst);
    @Query("""
        SELECT u
        FROM UsuarioModel u
        JOIN u.empleado e
        WHERE LOWER(e.correo) = LOWER(:correo)
           OR LOWER(u.correoInst) = LOWER(:correo)
        """)
    Optional<UsuarioModel> findByCorreoPersonalOInstitucional(
            @Param("correo") String correo
    );

    @Query("""
        SELECT u
        FROM UsuarioModel u
        LEFT JOIN FETCH u.empleado e
        WHERE LOWER(u.nombreUsuario) = LOWER(:identifier)
           OR LOWER(u.correoInst) = LOWER(:identifier)
           OR (e IS NOT NULL AND LOWER(e.correo) = LOWER(:identifier))
        """)
    Optional<UsuarioModel> findByIdentifier(
            @Param("identifier") String identifier
    );
}
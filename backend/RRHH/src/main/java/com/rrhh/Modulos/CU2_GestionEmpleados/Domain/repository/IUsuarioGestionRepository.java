package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;

public interface IUsuarioGestionRepository {

    UsuarioGestion save(UsuarioGestion usuario);

    UsuarioGestion findByEmpleadoId(Long idEmpleado);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByCorreoInst(String correoInst);
}
package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol;

public interface IRolRepository {

    Rol findByNombreRol(String nombreRol);
}
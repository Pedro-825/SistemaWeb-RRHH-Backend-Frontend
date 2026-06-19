package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.factory;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;

public interface IUsuarioFactory {
    Usuario crearUsuarioEmpleado(String nombreUsuario, String correoInst, String contrasenia,
                                  Boolean activo, Boolean dosfaActivo);
    Usuario crearUsuarioRrhh(String nombreUsuario, String correoInst, String contrasenia);
}

package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.factory;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioFactory implements IUsuarioFactory {

    @Override
    public Usuario crearUsuarioEmpleado(String nombreUsuario, String correoInst, String contrasenia,
                                         Boolean activo, Boolean dosfaActivo) {
        return Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .correoInst(correoInst)
                .contrasenia(contrasenia)
                .activo(activo)
                .dosFAActivado(dosfaActivo)
                .nombreRol("EMPLEADO")
                .build();
    }

    @Override
    public Usuario crearUsuarioRrhh(String nombreUsuario, String correoInst, String contrasenia) {
        return Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .correoInst(correoInst)
                .contrasenia(contrasenia)
                .activo(true)
                .dosFAActivado(true)
                .nombreRol("RRHH")
                .build();
    }
}

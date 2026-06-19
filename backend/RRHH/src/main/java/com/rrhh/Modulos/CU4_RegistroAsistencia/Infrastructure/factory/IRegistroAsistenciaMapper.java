package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.factory;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.RegistroAsistencia;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;

public interface IRegistroAsistenciaMapper {
    RegistroAsistenciaModel crearModeloDesdeDominio(RegistroAsistencia dominio);
    RegistroAsistencia crearDominioDesdeModelo(RegistroAsistenciaModel model);
}

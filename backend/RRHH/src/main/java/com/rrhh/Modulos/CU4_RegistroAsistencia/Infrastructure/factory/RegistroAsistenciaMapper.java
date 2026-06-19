package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.factory;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.Horario;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.RegistroAsistencia;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.stereotype.Component;

@Component
public class RegistroAsistenciaMapper implements IRegistroAsistenciaMapper {

    @Override
    public RegistroAsistenciaModel crearModeloDesdeDominio(RegistroAsistencia dominio) {
        if (dominio == null) {
            return null;
        }

        RegistroAsistenciaModel model = new RegistroAsistenciaModel();

        model.setIdRegistro(dominio.getIdRegistro());
        model.setFecha(dominio.getFecha());
        model.setHoraEntrada(dominio.getHoraEntrada());
        model.setHoraSalida(dominio.getHoraSalida());
        model.setHorasTrabajadas(dominio.getHorasTrabajadas());
        model.setMinutosTardanza(dominio.getMinutosTardanza());
        model.setMinutosExtra(dominio.getMinutosExtra());
        model.setEstado(dominio.getEstado());
        model.setTipoUltimoRegistro(dominio.getTipoUltimoRegistro());
        model.setObservacion(dominio.getObservacion());
        model.setCreadoEl(dominio.getCreadoEl());
        model.setActualizadoEl(dominio.getActualizadoEl());

        if (dominio.getEmpleado() != null && dominio.getEmpleado().getIdEmpleado() != null) {
            EmpleadoModel empleadoModel = new EmpleadoModel();
            empleadoModel.setIdEmpleado(dominio.getEmpleado().getIdEmpleado());
            empleadoModel.setNombres(dominio.getEmpleado().getNombres());
            empleadoModel.setApellidos(dominio.getEmpleado().getApellidos());
            model.setEmpleado(empleadoModel);
        }

        if (dominio.getHorario() != null && dominio.getHorario().getIdHorario() != null) {
            HorarioModel horarioModel = new HorarioModel();
            horarioModel.setIdHorario(dominio.getHorario().getIdHorario());
            horarioModel.setNombreTurno(dominio.getHorario().getNombreTurno());
            horarioModel.setHoraEntrada(dominio.getHorario().getHoraEntrada());
            horarioModel.setHoraSalida(dominio.getHorario().getHoraSalida());
            model.setHorario(horarioModel);
        }

        return model;
    }

    @Override
    public RegistroAsistencia crearDominioDesdeModelo(RegistroAsistenciaModel model) {
        if (model == null) {
            return null;
        }

        RegistroAsistencia dominio = new RegistroAsistencia();

        dominio.setIdRegistro(model.getIdRegistro());
        dominio.setFecha(model.getFecha());
        dominio.setHoraEntrada(model.getHoraEntrada());
        dominio.setHoraSalida(model.getHoraSalida());
        dominio.setHorasTrabajadas(model.getHorasTrabajadas());
        dominio.setMinutosTardanza(model.getMinutosTardanza());
        dominio.setMinutosExtra(model.getMinutosExtra());
        dominio.setEstado(model.getEstado());
        dominio.setTipoUltimoRegistro(model.getTipoUltimoRegistro());
        dominio.setObservacion(model.getObservacion());
        dominio.setCreadoEl(model.getCreadoEl());
        dominio.setActualizadoEl(model.getActualizadoEl());

        if (model.getEmpleado() != null) {
            Empleado empleado = new Empleado();
            empleado.setIdEmpleado(model.getEmpleado().getIdEmpleado());
            empleado.setNombres(model.getEmpleado().getNombres());
            empleado.setApellidos(model.getEmpleado().getApellidos());
            dominio.setEmpleado(empleado);
        }

        if (model.getHorario() != null) {
            Horario horario = new Horario();
            horario.setIdHorario(model.getHorario().getIdHorario());
            horario.setNombreTurno(model.getHorario().getNombreTurno());
            horario.setHoraEntrada(model.getHorario().getHoraEntrada());
            horario.setHoraSalida(model.getHorario().getHoraSalida());
            dominio.setHorario(horario);
        }

        return dominio;
    }
}

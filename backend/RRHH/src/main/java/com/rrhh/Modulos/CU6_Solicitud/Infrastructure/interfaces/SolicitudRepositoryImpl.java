package com.rrhh.Modulos.CU6_Solicitud.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.SolicitudModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;
import com.rrhh.Modulos.CU6_Solicitud.Domain.repository.ISolicitudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SolicitudRepositoryImpl implements ISolicitudRepository {

    private final JpaSolicitudRepository jpaSolicitudRepository;

    public SolicitudRepositoryImpl(JpaSolicitudRepository jpaSolicitudRepository) {
        this.jpaSolicitudRepository = jpaSolicitudRepository;
    }

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        SolicitudModel model = mapearAModel(solicitud);
        SolicitudModel guardado = jpaSolicitudRepository.save(Objects.requireNonNull(model));
        solicitud.setIdSolicitud(guardado.getIdSolicitud());
        return solicitud;
    }

    @Override
    public Optional<Solicitud> buscarPorId(Integer idSolicitud) {
        return jpaSolicitudRepository.findById(Objects.requireNonNull(idSolicitud))
            .map(this::mapearAEntidad);
    }

    @Override
    public List<Solicitud> buscarPorEmpleado(Long idEmpleado) {
        return jpaSolicitudRepository.findByEmpleadoIdEmpleado(idEmpleado)
            .stream().map(this::mapearAEntidad).collect(Collectors.toList());
    }

    @Override
    public List<Solicitud> buscarPorEstado(String estado) {
        return jpaSolicitudRepository.findByEstado(estado)
            .stream().map(this::mapearAEntidad).collect(Collectors.toList());
    }

    @Override
    public List<Solicitud> buscarTodas() {
        return jpaSolicitudRepository.findAll()
            .stream().map(this::mapearAEntidad).collect(Collectors.toList());
    }

    private SolicitudModel mapearAModel(Solicitud solicitud) {
        SolicitudModel model = new SolicitudModel();
        model.setIdSolicitud(solicitud.getIdSolicitud());
        model.setTipoSolicitud(solicitud.getTipoSolicitud());
        model.setFechaInicio(solicitud.getFechaInicio());
        model.setFechaFin(solicitud.getFechaFin());
        model.setDiasSolicitados(solicitud.getDiasSolicitados());
        model.setMotivo(solicitud.getMotivo());
        model.setEstado(solicitud.getEstado());
        model.setRespuesta(solicitud.getRespuesta());
        model.setObservacionRrhh(solicitud.getObservacionRrhh());
        model.setArchivoAdjunto(solicitud.getArchivoAdjunto());
        model.setFechaRevision(solicitud.getFechaRevision());
        model.setFechaDecision(solicitud.getFechaDecision());
        model.setCreadoEl(solicitud.getCreadoEl());
        model.setActualizadoEl(solicitud.getActualizadoEl());

        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(solicitud.getIdEmpleado());
        model.setEmpleado(emp);

        if (solicitud.getRevisadoPor() != null) {
            UsuarioModel revisado = new UsuarioModel();
            revisado.setIdUsuario(solicitud.getRevisadoPor());
            model.setRevisadoPor(revisado);
        }

        if (solicitud.getAprobadoPor() != null) {
            UsuarioModel aprobado = new UsuarioModel();
            aprobado.setIdUsuario(solicitud.getAprobadoPor());
            model.setAprobadoPor(aprobado);
        }

        return model;
    }

    private Solicitud mapearAEntidad(SolicitudModel model) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(model.getIdSolicitud());
        solicitud.setTipoSolicitud(model.getTipoSolicitud());
        solicitud.setFechaInicio(model.getFechaInicio());
        solicitud.setFechaFin(model.getFechaFin());
        solicitud.setDiasSolicitados(model.getDiasSolicitados());
        solicitud.setMotivo(model.getMotivo());
        solicitud.setEstado(model.getEstado());
        solicitud.setRespuesta(model.getRespuesta());
        solicitud.setObservacionRrhh(model.getObservacionRrhh());
        solicitud.setArchivoAdjunto(model.getArchivoAdjunto());
        solicitud.setFechaRevision(model.getFechaRevision());
        solicitud.setFechaDecision(model.getFechaDecision());
        solicitud.setCreadoEl(model.getCreadoEl());
        solicitud.setActualizadoEl(model.getActualizadoEl());
        if (model.getEmpleado() != null) solicitud.setIdEmpleado(model.getEmpleado().getIdEmpleado());
        if (model.getRevisadoPor() != null) solicitud.setRevisadoPor(model.getRevisadoPor().getIdUsuario());
        if (model.getAprobadoPor() != null) solicitud.setAprobadoPor(model.getAprobadoPor().getIdUsuario());
        return solicitud;
    }
}
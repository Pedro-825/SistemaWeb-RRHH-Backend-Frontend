package com.rrhh.Modulos.CU6_Solicitud.Application.services;

import com.rrhh.Modulos.CU6_Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.SolicitudResponseDTO;

import java.util.List;

public interface ISolicitudService {

    SolicitudResponseDTO registrarSolicitud(RegistrarSolicitudRequestDTO request, Long idEmpleado);

    SolicitudResponseDTO revisarSolicitudRrhh(RevisionRrhhRequestDTO request, Long idUsuarioRrhh);

    SolicitudResponseDTO decidirSolicitudGerencia(DecisionGerenciaRequestDTO request, Long idUsuarioGerencia);

    List<SolicitudResponseDTO> listarPorEstado(String estado);

    List<SolicitudResponseDTO> listarPorEmpleado(Long idEmpleado);

    SolicitudResponseDTO buscarPorId(Integer idSolicitud);

    SolicitudResponseDTO clonarSolicitud(Integer idSolicitud, Long idEmpleado);

    int obtenerSaldoVacaciones(Long idUsuario);
}
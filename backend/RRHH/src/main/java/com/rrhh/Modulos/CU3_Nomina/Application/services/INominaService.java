package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.AjustarNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;

import java.util.List;

public interface INominaService {

    List<NominaResponseDTO> calcularNomina(CalcularNominaRequestDTO request, Long idUsuarioRrhh);

    NominaResponseDTO buscarNominaPorId(Integer idNomina);

    List<NominaResponseDTO> buscarPorPeriodo(String periodo);

    List<NominaResponseDTO> buscarPorEmpleado(Long idEmpleado);

    List<NominaResponseDTO> buscarPorNombreEmpleado(String nombre);

    NominaResponseDTO ajustarNomina(Integer idNomina, AjustarNominaRequestDTO request, Long idUsuarioRrhh);
}

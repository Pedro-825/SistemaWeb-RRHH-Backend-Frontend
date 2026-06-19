package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEmpleadoService {

    EmpleadoResponseDTO registrarEmpleado(
            RegistrarEmpleadoRequestDTO dto
    );

    List<BuscarEmpleadoResponseDTO> listarEmpleados();

    Page<BuscarEmpleadoResponseDTO> listarEmpleados(Pageable pageable);

    List<BuscarEmpleadoResponseDTO> buscarEmpleados(String filtro);

    Page<BuscarEmpleadoResponseDTO> buscarEmpleados(String filtro, Pageable pageable);
    EmpleadoResponseDTO actualizarEmpleado(
        Long idEmpleado,
        ActualizarEmpleadoRequestDTO dto
    );

    EmpleadoResponseDTO desactivarEmpleado(
            Long idEmpleado,
            DesactivarEmpleadoRequestDTO dto
    );
    EmpleadoResponseDTO registrarSancion(
        Long idEmpleado,
        RegistrarSancionRequestDTO dto
);
EmpleadoResponseDTO reactivarEmpleado(
        Long idEmpleado,
        ReactivarEmpleadoRequestDTO dto
);
EmpleadoResponseDTO registrarAscenso(
        Long idEmpleado,
        RegistrarAscensoRequestDTO dto
);
EmpleadoResponseDTO registrarCambioSalarial(
        Long idEmpleado,
        CambioSalarialRequestDTO dto
);
List<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
        String estado,
        Integer idDpto,
        String cargo
);

Page<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
        String estado,
        Integer idDpto,
        String cargo,
        Pageable pageable
);
}
package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoService implements IEmpleadoService {

    private final RegistrarEmpleadoUseCase registrarEmpleadoUseCase;

    private final BuscarEmpleadoUseCase buscarEmpleadoUseCase;

    private final ActualizarEmpleadoUseCase actualizarEmpleadoUseCase;

    private final DesactivarEmpleadoUseCase desactivarEmpleadoUseCase;

    private final RegistrarSancionUseCase registrarSancionUseCase;

    private final ReactivarEmpleadoUseCase reactivarEmpleadoUseCase;

    private final RegistrarAscensoUseCase registrarAscensoUseCase;

    private final CambioSalarialUseCase cambioSalarialUseCase;

    public EmpleadoService(RegistrarEmpleadoUseCase registrarEmpleadoUseCase, BuscarEmpleadoUseCase buscarEmpleadoUseCase, ActualizarEmpleadoUseCase actualizarEmpleadoUseCase, DesactivarEmpleadoUseCase desactivarEmpleadoUseCase, RegistrarSancionUseCase registrarSancionUseCase, ReactivarEmpleadoUseCase reactivarEmpleadoUseCase, RegistrarAscensoUseCase registrarAscensoUseCase, CambioSalarialUseCase cambioSalarialUseCase) {
        this.registrarEmpleadoUseCase = registrarEmpleadoUseCase;
        this.buscarEmpleadoUseCase = buscarEmpleadoUseCase;
        this.actualizarEmpleadoUseCase = actualizarEmpleadoUseCase;
        this.desactivarEmpleadoUseCase = desactivarEmpleadoUseCase;
        this.registrarSancionUseCase = registrarSancionUseCase;
        this.reactivarEmpleadoUseCase = reactivarEmpleadoUseCase;
        this.registrarAscensoUseCase = registrarAscensoUseCase;
        this.cambioSalarialUseCase = cambioSalarialUseCase;
    }

    @Override
    public EmpleadoResponseDTO registrarEmpleado(
            RegistrarEmpleadoRequestDTO dto
    ) {
        return registrarEmpleadoUseCase.ejecutar(dto);
    }

    @Override
    public List<BuscarEmpleadoResponseDTO> listarEmpleados() {
        return buscarEmpleadoUseCase.listar();
    }

    @Override
    public Page<BuscarEmpleadoResponseDTO> listarEmpleados(Pageable pageable) {
        return buscarEmpleadoUseCase.listar(pageable);
    }

    @Override
    public List<BuscarEmpleadoResponseDTO> buscarEmpleados(
            String filtro
    ) {
        return buscarEmpleadoUseCase.buscarPorFiltro(filtro);
    }

    @Override
    public Page<BuscarEmpleadoResponseDTO> buscarEmpleados(
            String filtro,
            Pageable pageable
    ) {
        return buscarEmpleadoUseCase.buscarPorFiltro(filtro, pageable);
    }

    @Override
    public List<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
            String estado,
            Integer idDpto,
            String cargo
    ) {
        return buscarEmpleadoUseCase.buscarAvanzado(
                estado,
                idDpto,
                cargo
        );
    }

    @Override
    public Page<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
            String estado,
            Integer idDpto,
            String cargo,
            Pageable pageable
    ) {
        return buscarEmpleadoUseCase.buscarAvanzado(
                estado,
                idDpto,
                cargo,
                pageable
        );
    }

    @Override
    public EmpleadoResponseDTO actualizarEmpleado(
            Long idEmpleado,
            ActualizarEmpleadoRequestDTO dto
    ) {
        return actualizarEmpleadoUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO desactivarEmpleado(
            Long idEmpleado,
            DesactivarEmpleadoRequestDTO dto
    ) {
        return desactivarEmpleadoUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO registrarSancion(
            Long idEmpleado,
            RegistrarSancionRequestDTO dto
    ) {
        return registrarSancionUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO reactivarEmpleado(
            Long idEmpleado,
            ReactivarEmpleadoRequestDTO dto
    ) {
        return reactivarEmpleadoUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO registrarAscenso(
            Long idEmpleado,
            RegistrarAscensoRequestDTO dto,
            Long idEmpleadoActor,
            String rolActor
    ) {
        return registrarAscensoUseCase.ejecutar(
                idEmpleado,
                dto,
                idEmpleadoActor,
                rolActor
        );
    }

    @Override
    public EmpleadoResponseDTO registrarCambioSalarial(
            Long idEmpleado,
            CambioSalarialRequestDTO dto
    ) {
        return cambioSalarialUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }
}

package com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.controllers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.IEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.routes.EmpleadoApiRoutes;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaHistorialRepository;
import com.rrhh.Shared.persistence.HistorialAuditoriaModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping(EmpleadoApiRoutes.EMPLEADO_BASE)
public class EmpleadoController {

    private final IEmpleadoService empleadoService;
    private final JpaHistorialRepository historialRepository;

    public EmpleadoController(IEmpleadoService empleadoService, JpaHistorialRepository historialRepository) {
        this.empleadoService = empleadoService;
        this.historialRepository = historialRepository;
    }

    @PostMapping(EmpleadoApiRoutes.REGISTRAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarEmpleado(
            @Valid @RequestBody RegistrarEmpleadoRequestDTO dto
    ) {
        return empleadoService.registrarEmpleado(dto);
    }

    @GetMapping(EmpleadoApiRoutes.LISTAR)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public Page<BuscarEmpleadoResponseDTO> listarEmpleados(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return empleadoService.listarEmpleados(pageable);
    }

    @GetMapping(EmpleadoApiRoutes.BUSCAR)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public Page<BuscarEmpleadoResponseDTO> buscarEmpleados(
            @RequestParam String filtro,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return empleadoService.buscarEmpleados(filtro, pageable);
    }

    @GetMapping(EmpleadoApiRoutes.BUSCAR_AVANZADO)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public Page<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idDpto,
            @RequestParam(required = false) String cargo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return empleadoService.buscarEmpleadosAvanzado(
                estado,
                idDpto,
                cargo,
                pageable
        );
    }

    @PutMapping(EmpleadoApiRoutes.ACTUALIZAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO actualizarEmpleado(
            @PathVariable Long idEmpleado,
            @Valid @RequestBody ActualizarEmpleadoRequestDTO dto
    ) {
        return empleadoService.actualizarEmpleado(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.DESACTIVAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO desactivarEmpleado(
            @PathVariable Long idEmpleado,
            @RequestBody DesactivarEmpleadoRequestDTO dto
    ) {
        return empleadoService.desactivarEmpleado(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.SANCION)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarSancion(
            @PathVariable Long idEmpleado,
            @Valid @RequestBody RegistrarSancionRequestDTO dto
    ) {
        return empleadoService.registrarSancion(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.REACTIVAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO reactivarEmpleado(
            @PathVariable Long idEmpleado,
            @RequestBody ReactivarEmpleadoRequestDTO dto
    ) {
        return empleadoService.reactivarEmpleado(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.ASCENSO)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarAscenso(
            @PathVariable Long idEmpleado,
            @RequestBody RegistrarAscensoRequestDTO dto
    ) {
        return empleadoService.registrarAscenso(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.CAMBIO_SALARIAL)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarCambioSalarial(
            @PathVariable Long idEmpleado,
            @RequestBody CambioSalarialRequestDTO dto
    ) {
        return empleadoService.registrarCambioSalarial(idEmpleado, dto);
    }

    @GetMapping(EmpleadoApiRoutes.HISTORIAL)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public ResponseEntity<List<Map<String, Object>>> getHistorialEmpleado(
            @PathVariable Long idEmpleado) {
        List<HistorialAuditoriaModel> historial = historialRepository
                .findByIdRegistroAfectadoAndTablaAfectada(
                        idEmpleado, "EMPLEADO",
                        Sort.by(Sort.Direction.DESC, "fechaDeCambio"));
        List<Map<String, Object>> result = historial.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idAuditoria", h.getIdAuditoria());
            m.put("accion", h.getAccion());
            m.put("tablaAfectada", h.getTablaAfectada());
            m.put("valorAnterior", h.getValorAnterior());
            m.put("valorNuevo", h.getValorNuevo());
            m.put("ipMaquina", h.getIpMaquina());
            m.put("fechaDeCambio", h.getFechaDeCambio());
            m.put("creadoEl", h.getCreadoEl());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
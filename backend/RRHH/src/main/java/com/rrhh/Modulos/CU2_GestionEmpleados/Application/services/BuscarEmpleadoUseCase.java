package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BuscarEmpleadoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    public BuscarEmpleadoUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IUsuarioGestionRepository usuarioRepository) {
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<BuscarEmpleadoResponseDTO> listar() {

        List<Empleado> empleados =
                empleadoRepository.findAll();

        return convertirLista(empleados);
    }

    public List<BuscarEmpleadoResponseDTO> buscarPorFiltro(String filtro) {

        List<Empleado> empleados =
                empleadoRepository.buscarPorFiltro(filtro);

        return convertirLista(empleados);
    }

    public List<BuscarEmpleadoResponseDTO> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo
    ) {

        String estadoFiltro = null;
        String cargoFiltro = null;

        if (estado != null && !estado.isBlank()) {
            estadoFiltro = estado.toUpperCase();
        }

        if (cargo != null && !cargo.isBlank()) {
            cargoFiltro = cargo;
        }

        List<Empleado> empleados =
                empleadoRepository.buscarAvanzado(
                        estadoFiltro,
                        idDpto,
                        cargoFiltro
                );

        return convertirLista(empleados);
    }

    public Page<BuscarEmpleadoResponseDTO> listar(Pageable pageable) {

        Page<Empleado> page =
                empleadoRepository.findAll(pageable);

        return page.map(this::convertir);
    }

    public Page<BuscarEmpleadoResponseDTO> buscarPorFiltro(String filtro, Pageable pageable) {

        Page<Empleado> page =
                empleadoRepository.buscarPorFiltro(filtro, pageable);

        return page.map(this::convertir);
    }

    public Page<BuscarEmpleadoResponseDTO> buscarAvanzado(
            String estado,
            Integer idDpto,
            String cargo,
            Pageable pageable
    ) {

        String estadoFiltro = null;
        String cargoFiltro = null;

        if (estado != null && !estado.isBlank()) {
            estadoFiltro = estado.toUpperCase();
        }

        if (cargo != null && !cargo.isBlank()) {
            cargoFiltro = cargo;
        }

        Page<Empleado> page =
                empleadoRepository.buscarAvanzado(
                        estadoFiltro,
                        idDpto,
                        cargoFiltro,
                        pageable
                );

        return page.map(this::convertir);
    }

    private List<BuscarEmpleadoResponseDTO> convertirLista(
            List<Empleado> empleados
    ) {

        List<BuscarEmpleadoResponseDTO> respuesta =
                new ArrayList<>();

        for (Empleado empleado : empleados) {
            respuesta.add(convertir(empleado));
        }

        return respuesta;
    }

    private BuscarEmpleadoResponseDTO convertir(
            Empleado empleado
    ) {

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(
                        empleado.getIdEmpleado()
                );

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(
                        empleado.getIdEmpleado()
                );

        return new BuscarEmpleadoResponseDTO(
                empleado.getIdEmpleado(),
                empleado.getNombres(),
                empleado.getApellidos(),
                empleado.getNumeroDi(),
                empleado.getCorreo(),
                empleado.getTelefono(),
                empleado.getEstado(),
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contrato != null ? contrato.getSueldo() : null,
                contrato != null ? contrato.getFechaInicio() : null,
                contrato != null ? contrato.getTipoContrato() : null,
                contrato != null ? contrato.getIdDpto() : null,
                empleado.getDireccion()
        );
    }
}
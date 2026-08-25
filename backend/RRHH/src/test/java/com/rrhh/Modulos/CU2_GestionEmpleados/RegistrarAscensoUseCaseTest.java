package com.rrhh.Modulos.CU2_GestionEmpleados;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.RegistrarAscensoUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RegistrarAscensoUseCaseTest {

    private IEmpleadoRepository empleadoRepository;
    private IContratoRepository contratoRepository;
    private IUsuarioGestionRepository usuarioRepository;
    private IRolRepository rolRepository;
    private RegistrarAscensoUseCase useCase;

    @BeforeEach
    void setUp() {
        empleadoRepository = mock(IEmpleadoRepository.class);
        contratoRepository = mock(IContratoRepository.class);
        IDepartamentoRepository departamentoRepository = mock(IDepartamentoRepository.class);
        usuarioRepository = mock(IUsuarioGestionRepository.class);
        rolRepository = mock(IRolRepository.class);
        AuditoriaEmpleadoService auditoriaEmpleadoService = mock(AuditoriaEmpleadoService.class);

        useCase = new RegistrarAscensoUseCase(
                empleadoRepository,
                contratoRepository,
                departamentoRepository,
                usuarioRepository,
                rolRepository,
                auditoriaEmpleadoService
        );
    }

    @Test
    void bloqueaCambioDeRolPropio() {
        Empleado empleado = empleadoActivo(10L);
        when(empleadoRepository.findById(10L)).thenReturn(empleado);
        when(usuarioRepository.findByEmpleadoId(10L)).thenReturn(usuario(10L, "RRHH"));
        when(rolRepository.findByNombreRol("EMPLEADO")).thenReturn(rol("EMPLEADO"));

        RegistrarAscensoRequestDTO dto = new RegistrarAscensoRequestDTO();
        dto.setNuevoRol("EMPLEADO");

        EmpleadoResponseDTO respuesta = useCase.ejecutar(10L, dto, 10L, "RRHH");

        assertThat(respuesta.isSuccess()).isFalse();
        assertThat(respuesta.getMessage()).contains("propio rol");
        verifyNoInteractions(contratoRepository);
    }

    @Test
    void bloqueaQueRrhhModifiqueCargoDeOtroRrhh() {
        Empleado empleado = empleadoActivo(20L);
        when(empleadoRepository.findById(20L)).thenReturn(empleado);
        when(usuarioRepository.findByEmpleadoId(20L)).thenReturn(usuario(20L, "RRHH"));
        when(rolRepository.findByNombreRol("RRHH")).thenReturn(rol("RRHH"));

        RegistrarAscensoRequestDTO dto = new RegistrarAscensoRequestDTO();
        dto.setNuevoRol("RRHH");
        dto.setNuevoCargo("Auxiliar RRHH");

        EmpleadoResponseDTO respuesta = useCase.ejecutar(20L, dto, 10L, "RRHH");

        assertThat(respuesta.isSuccess()).isFalse();
        assertThat(respuesta.getMessage()).contains("otro usuario RRHH");
        verifyNoInteractions(contratoRepository);
    }

    private Empleado empleadoActivo(Long idEmpleado) {
        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(idEmpleado);
        empleado.setNombres("Empleado");
        empleado.setApellidos("Prueba");
        empleado.setEstado("ACTIVO");
        return empleado;
    }

    private UsuarioGestion usuario(Long idEmpleado, String nombreRol) {
        UsuarioGestion usuario = new UsuarioGestion();
        usuario.setIdEmpleado(idEmpleado);
        usuario.setNombreRol(nombreRol);
        return usuario;
    }

    private Rol rol(String nombreRol) {
        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombreRol(nombreRol);
        rol.setActivo(true);
        return rol;
    }
}

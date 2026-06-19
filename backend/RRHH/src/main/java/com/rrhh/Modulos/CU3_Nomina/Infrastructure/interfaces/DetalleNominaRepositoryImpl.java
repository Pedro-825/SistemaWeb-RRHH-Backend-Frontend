package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;

import com.rrhh.Modulos.CU3_Nomina.Domain.entities.DetalleNomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Shared.persistence.DetalleNominaModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.NominaModel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class DetalleNominaRepositoryImpl implements IDetalleNominaRepository {

    private final JpaDetalleNominaRepository jpaDetalleNominaRepository;

    public DetalleNominaRepositoryImpl(JpaDetalleNominaRepository jpaDetalleNominaRepository) {
        this.jpaDetalleNominaRepository = jpaDetalleNominaRepository;
    }

    @Override
    public DetalleNomina guardar(DetalleNomina detalle) {
        DetalleNominaModel model = mapearAModel(detalle);
        jpaDetalleNominaRepository.save(Objects.requireNonNull(model));
        return detalle;
    }

    @Override
    public List<DetalleNomina> guardarTodos(List<DetalleNomina> detalles) {
        List<DetalleNominaModel> models = detalles.stream()
            .map(this::mapearAModel)
            .collect(Collectors.toList());
        jpaDetalleNominaRepository.saveAll(Objects.requireNonNull(models));
        return detalles;
    }

    @Override
    public List<DetalleNomina> buscarPorNomina(Integer idNomina) {
        return jpaDetalleNominaRepository.findByNominaIdNomina(idNomina)
            .stream().map(this::mapearAEntidad).collect(Collectors.toList());
    }

    @Override
    public List<DetalleNomina> buscarPorEmpleado(Long idEmpleado) {
        return jpaDetalleNominaRepository.findByEmpleadoIdEmpleado(idEmpleado)
            .stream().map(this::mapearAEntidad).collect(Collectors.toList());
    }

    @Override
    public void eliminarPorNomina(Integer idNomina) {
        jpaDetalleNominaRepository.deleteByNominaIdNomina(idNomina);
    }

    private DetalleNominaModel mapearAModel(DetalleNomina detalle) {
        DetalleNominaModel model = new DetalleNominaModel();
        model.setFecha(detalle.getFecha());
        model.setHorasTrabajadas(detalle.getHorasTrabajadas());
        model.setMinutosTardanza(detalle.getMinutosTardanza());
        model.setMinutosExtra(detalle.getMinutosExtra());
        model.setMontoHoraExtra(detalle.getMontoHoraExtra());
        model.setDescuentoTardanza(detalle.getDescuentoTardanza());
        model.setObservacion(detalle.getObservacion());

        NominaModel nomina = new NominaModel();
        nomina.setIdNomina(detalle.getIdNomina());
        model.setNomina(nomina);

        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(detalle.getIdEmpleado());
        model.setEmpleado(emp);

        return model;
    }

    DetalleNomina mapearAEntidad(DetalleNominaModel model) {
        DetalleNomina detalle = new DetalleNomina();
        detalle.setIdDetalle(model.getIdDetalle());
        detalle.setFecha(model.getFecha());
        detalle.setHorasTrabajadas(model.getHorasTrabajadas());
        detalle.setMinutosTardanza(model.getMinutosTardanza());
        detalle.setMinutosExtra(model.getMinutosExtra());
        detalle.setMontoHoraExtra(model.getMontoHoraExtra());
        detalle.setDescuentoTardanza(model.getDescuentoTardanza());
        detalle.setObservacion(model.getObservacion());
        if (model.getNomina() != null) detalle.setIdNomina(model.getNomina().getIdNomina());
        if (model.getEmpleado() != null) detalle.setIdEmpleado(model.getEmpleado().getIdEmpleado());
        return detalle;
    }
}
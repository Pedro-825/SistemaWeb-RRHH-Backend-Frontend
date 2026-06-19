package com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.routes;

public class EmpleadoApiRoutes {

    public static final String EMPLEADO_BASE = "/api/v1/empleados";

    public static final String REGISTRAR = "/registrar";
    public static final String LISTAR = "/listar";
    public static final String BUSCAR = "/buscar";

    public static final String ACTUALIZAR = "/{idEmpleado}";
    public static final String DESACTIVAR = "/{idEmpleado}/desactivar";
    public static final String REACTIVAR = "/{idEmpleado}/reactivar";

    public static final String SANCION = "/{idEmpleado}/sancion";
    public static final String ASCENSO = "/{idEmpleado}/ascenso";
    public static final String CAMBIO_SALARIAL = "/{idEmpleado}/cambio-salarial";
    public static final String BUSCAR_AVANZADO = "/buscar-avanzado";
    public static final String HISTORIAL = "/{idEmpleado}/historial";
}
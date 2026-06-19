package com.rrhh.Modulos.CU6_Solicitud.Presentation.routes;

public class SolicitudApiRoutes {

    private SolicitudApiRoutes() {}

    public static final String BASE             = "/api/solicitud";
    public static final String REGISTRAR        = BASE + "/registrar";
    public static final String REVISAR_RRHH     = BASE + "/revisar";
    public static final String DECIDIR_GERENCIA = BASE + "/decidir";
    public static final String POR_ESTADO       = BASE + "/estado/{estado}";
    public static final String POR_EMPLEADO     = BASE + "/empleado/{idEmpleado}";
    public static final String BUSCAR_ID        = BASE + "/{id}";
    public static final String CLONAR           = BASE + "/{id}/clonar";
}
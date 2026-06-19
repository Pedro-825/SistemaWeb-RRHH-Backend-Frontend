package com.rrhh.Modulos.CU3_Nomina.Presentation.routes;

public class NominaApiRoutes {

    private NominaApiRoutes() {}

    public static final String BASE = "/api/nomina";

    public static final String CALCULAR   = BASE + "/calcular";
    public static final String BUSCAR_ID  = BASE + "/{id}";
    public static final String AJUSTAR = BASE + "/{id}/ajustar";
    public static final String POR_PERIODO = BASE + "/periodo/{periodo}";
    public static final String POR_EMPLEADO = BASE + "/empleado/{idEmpleado}";
    public static final String POR_NOMBRE = BASE + "/buscar";
}

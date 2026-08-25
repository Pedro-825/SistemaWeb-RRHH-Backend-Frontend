/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  MOCK INTERCEPTOR — ELIMINAR ANTES DE PRODUCCIÓN               ║
 * ║  Intercepta llamadas a localhost:8080 y devuelve datos falsos   ║
 * ║  Solo activo cuando sessionStorage.devMode === 'true'           ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */

const mocksActivados = import.meta.env.VITE_ENABLE_MOCKS === 'true' || sessionStorage.getItem('devMode') === 'true';
if (mocksActivados) {

const DELAY = 280; // ms de latencia simulada

/* ══════════════════════════════════════════════════════════════════
   DATOS DE PRUEBA
══════════════════════════════════════════════════════════════════ */

const E = (id, nom, ape, dni, cargo, depto, idDpto, sueldo, estado, contrato, inicio, correo, tel) => ({
  idEmpleado: id, nombres: nom, apellidos: ape, numeroDi: dni, docIdentidad: "DNI",
  cargo, departamento: depto, idDpto, sueldo, estado,
  tipoContrato: contrato, fechaInicio: inicio, correo, telefono: tel,
});

const EMPLEADOS = [
  E(1,  "Ana",      "García López",    "12345678", "Enfermera Jefe",          "Enfermería",     1, 3200, "ACTIVO",   "INDEFINIDO", "2020-03-15", "ana.garcia@hospital.pe",     "987654321"),
  E(2,  "Carlos",   "Méndez Torres",   "23456789", "Médico General",          "Medicina",       2, 5500, "ACTIVO",   "INDEFINIDO", "2018-06-01", "c.mendez@hospital.pe",       "976543210"),
  E(3,  "María",    "Quispe Riva",     "34567890", "Administradora",          "Administración", 3, 3200, "ACTIVO",   "PLAZO_FIJO", "2021-01-10", "m.quispe@hospital.pe",       "965432109"),
  E(4,  "Luis",     "Fernández Paz",   "45678901", "Técnico de Laboratorio",  "Laboratorio",    4, 2400, "ACTIVO",   "INDEFINIDO", "2019-09-20", "l.fernandez@hospital.pe",    "954321098"),
  E(5,  "Rosa",     "Vargas Chávez",   "56789012", "Enfermera",               "Enfermería",     1, 2800, "ACTIVO",   "INDEFINIDO", "2022-02-14", "r.vargas@hospital.pe",       "943210987"),
  E(6,  "Pedro",    "Ramos Soto",      "67890123", "Médico Especialista",     "Medicina",       2, 7200, "ACTIVO",   "INDEFINIDO", "2017-11-05", "p.ramos@hospital.pe",        "932109876"),
  E(7,  "Elena",    "Castro Neira",    "78901234", "Contadora",               "Finanzas",       5, 3800, "ACTIVO",   "INDEFINIDO", "2020-07-22", "e.castro@hospital.pe",       "921098765"),
  E(8,  "Jorge",    "Huamán Pinto",    "89012345", "Enfermero",               "Enfermería",     1, 2600, "INACTIVO", "PLAZO_FIJO", "2019-04-08", "j.huaman@hospital.pe",       "910987654"),
  E(9,  "Sofía",    "Delgado Ríos",    "90123456", "Recepcionista",           "Administración", 3, 2200, "ACTIVO",   "INDEFINIDO", "2023-01-03", "s.delgado@hospital.pe",      "909876543"),
  E(10, "Miguel",   "Paredes Luna",    "01234567", "Radiólogo",               "Radiología",     6, 6500, "ACTIVO",   "INDEFINIDO", "2016-08-15", "m.paredes@hospital.pe",      "998765432"),
  E(11, "Carmen",   "Flores Vera",     "11223344", "Nutricionista",           "Nutrición",      7, 3100, "ACTIVO",   "PLAZO_FIJO", "2021-05-20", "c.flores@hospital.pe",       "987654322"),
  E(12, "Roberto",  "Silva Mora",      "22334455", "Médico General",          "Medicina",       2, 5500, "ACTIVO",   "INDEFINIDO", "2020-10-12", "r.silva@hospital.pe",        "976543211"),
  E(13, "Patricia", "Rojas Campos",    "33445566", "Psicóloga",               "Psicología",     8, 3600, "ACTIVO",   "INDEFINIDO", "2019-03-28", "p.rojas@hospital.pe",        "965432108"),
  E(14, "Andrés",   "Vega Torres",     "44556677", "Fisioterapeuta",          "Rehabilitación", 9, 3300, "INACTIVO", "SERVICIOS",  "2018-12-01", "a.vega@hospital.pe",         "954321097"),
  E(15, "Lucía",    "Mendoza Cruz",    "55667788", "Auxiliar de Enfermería",  "Enfermería",     1, 2000, "ACTIVO",   "PLAZO_FIJO", "2023-06-15", "l.mendoza@hospital.pe",      "943210986"),
];

/* ── Justificaciones pendientes (vista RRHH) ── */
const JUSTIF_PENDIENTES = [
  { id: 101, idEmpleado: 3,  fechaRegistro: "2026-06-12", motivo: "Problema con el transporte público — accidente vial en Av. Arequipa.",     estado: "PENDIENTE" },
  { id: 102, idEmpleado: 5,  fechaRegistro: "2026-06-11", motivo: "Cita médica urgente de emergencia, adjunto certificado del centro de salud.", estado: "PENDIENTE" },
  { id: 103, idEmpleado: 9,  fechaRegistro: "2026-06-12", motivo: "Emergencia familiar imprevista, me avisaron a última hora.",                  estado: "PENDIENTE" },
  { id: 104, idEmpleado: 12, fechaRegistro: "2026-06-10", motivo: "Interrupción del servicio de buses en mi zona de residencia.",               estado: "PENDIENTE" },
  { id: 105, idEmpleado: 15, fechaRegistro: "2026-06-13", motivo: "Lluvia intensa que impidió salir a tiempo de casa.",                         estado: "PENDIENTE" },
];

/* ── Justificaciones del empleado #1 ── */
const JUSTIF_EMP1 = [
  { id: 201, idEmpleado: 1, idAsistencia: 301, fechaRegistro: "2026-06-10", tipo: "TARDANZA",     motivo: "Tráfico intenso en Av. Javier Prado, cierre por accidente de tránsito.", estado: "PENDIENTE",  evidencia: null, comentario: null },
  { id: 202, idEmpleado: 1, idAsistencia: 298, fechaRegistro: "2026-05-28", tipo: "TARDANZA",     motivo: "Problema con mi vehículo a mitad del trayecto.",                          estado: "APROBADO",   evidencia: null, comentario: "Justificación aceptada. Queda registrado." },
  { id: 203, idEmpleado: 1, idAsistencia: 275, fechaRegistro: "2026-05-14", tipo: "INASISTENCIA", motivo: "No presenté los documentos de respaldo necesarios.",                      estado: "RECHAZADO",  evidencia: null, comentario: "No cumple requisitos mínimos. Sin certificado adjunto." },
];

/* ── Nómina ── */
const hoy = new Date();
const mkNomina = (emp, offset = 0) => {
  const d    = new Date(hoy.getFullYear(), hoy.getMonth() - offset, 1);
  const mes  = String(d.getMonth() + 1).padStart(2, "0");
  const anio = String(d.getFullYear());
  const b    = +(emp.sueldo * 0.12).toFixed(2);
  const desc = +(emp.sueldo * 0.04).toFixed(2);
  return {
    idNomina: 1000 + emp.idEmpleado + offset * 20,
    idEmpleado: emp.idEmpleado,
    empleadoNombre: `${emp.nombres} ${emp.apellidos}`,
    mes, anio,
    sueldoBase: emp.sueldo,
    bonificaciones: b,
    descuentos: desc,
    sueldoNeto: +(emp.sueldo + b - desc).toFixed(2),
    estado: "CALCULADO",
  };
};

const NOMINAS_PERIODO  = EMPLEADOS.map(e => mkNomina(e, 0));
const NOMINAS_EMP1     = [0, 1, 2, 3, 4, 5].map(i => mkNomina(EMPLEADOS[0], i));

/* ── Solicitudes ── */
const mkSol = (id, idEmp, nombre, tipo, estado, ini, fin, dias, desc, rrhh = null, ger = null) => ({
  idSolicitud: id, idEmpleado: idEmp, nombreEmpleado: nombre, tipo,
  fechaInicio: ini, fechaFin: fin, diasSolicitados: dias, descripcion: desc, estado,
  comentarioRRHH: rrhh, respuestaGerencia: ger,
});

const SOL_PENDIENTE = [
  mkSol(501, 1,  "Ana García López",    "VACACIONES", "PENDIENTE", "2026-07-01", "2026-07-15", 15, "Vacaciones anuales programadas."),
  mkSol(502, 3,  "María Quispe Riva",   "PERMISO",    "PENDIENTE", "2026-06-20", "2026-06-20",  1, "Trámite notarial urgente, requiero el día libre."),
  mkSol(503, 5,  "Rosa Vargas Chávez",  "PERMISO",    "PENDIENTE", "2026-06-18", "2026-06-19",  2, "Consulta médica especialista y exámenes de laboratorio."),
  mkSol(504, 12, "Roberto Silva Mora",  "VACACIONES", "PENDIENTE", "2026-08-01", "2026-08-10", 10, "Vacaciones familiares planificadas."),
];

const SOL_EN_REVISION = [
  mkSol(505, 6,  "Pedro Ramos Soto",     "LICENCIA",   "EN_REVISION", "2026-07-05", "2026-07-12",  8, "Licencia por paternidad.",          "Fechas y documentos verificados, todo correcto."),
  mkSol(506, 7,  "Elena Castro Neira",   "PERMISO",    "EN_REVISION", "2026-06-25", "2026-06-25",  1, "Acto escolar del hijo menor.",      "Motivo válido, enviar a Gerencia para aprobación."),
  mkSol(507, 13, "Patricia Rojas Campos","VACACIONES", "EN_REVISION", "2026-09-01", "2026-09-14", 14, "Viaje familiar al exterior.", "Sin inconsistencias, todo en orden."),
];

const SOL_APROBADO = [
  mkSol(508, 2,  "Carlos Méndez Torres", "PERMISO",    "APROBADO", "2026-05-10", "2026-05-10", 1,  "Operación programada con antelación.", "Aprobado.",           "Autorizado, que se recupere pronto."),
  mkSol(509, 10, "Miguel Paredes Luna",  "VACACIONES", "APROBADO", "2026-04-01", "2026-04-10", 10, "Vacaciones anuales.",                  "Todo correcto.",      "Aprobado."),
  mkSol(510, 4,  "Luis Fernández Paz",   "LICENCIA",   "APROBADO", "2026-03-15", "2026-03-20", 6,  "Licencia médica por intervención.",    "Certificado válido.", "Autorizado."),
];

const SOL_RECHAZADO = [
  mkSol(511, 11, "Carmen Flores Vera",  "LICENCIA",  "RECHAZADO", "2026-06-01", "2026-06-07", 7, "Licencia sin goce de haber.",  "No procede en este período.",   "Período de alta demanda operativa, no es posible aprobar."),
  mkSol(512, 15, "Lucía Mendoza Cruz",  "OTROS",     "RECHAZADO", "2026-05-20", "2026-05-20", 1, "Motivo personal sin detallar.", "Documentación incompleta.",    "Solicitud incompleta, falta documentación de respaldo."),
];

const SOL_EMP1 = [
  mkSol(501, 1, "Ana García López", "VACACIONES", "PENDIENTE",  "2026-07-01", "2026-07-15", 15, "Vacaciones anuales programadas."),
  mkSol(498, 1, "Ana García López", "PERMISO",    "APROBADO",   "2026-05-15", "2026-05-15",  1, "Trámite bancario urgente.", "Todo correcto.", "Autorizado."),
  mkSol(490, 1, "Ana García López", "PERMISO",    "RECHAZADO",  "2026-04-20", "2026-04-21",  2, "Consulta médica, sin certificado adjunto.", "Documentación incompleta.", "Sin respaldo documental, rechazado."),
];

// Solicitudes dinámicas agregadas durante la sesión mock
const SOL_NUEVAS = [];

const SOL_BY_ESTADO = {
  PENDIENTE:   SOL_PENDIENTE,
  EN_REVISION: SOL_EN_REVISION,
  APROBADO:    SOL_APROBADO,
  RECHAZADO:   SOL_RECHAZADO,
  PROCESADO:   SOL_EN_REVISION,
};

/* ── Reportes ── */
const REPORTE_EMPLEADOS = EMPLEADOS.map(e => ({
  "ID": e.idEmpleado,
  "Nombres":          `${e.nombres} ${e.apellidos}`,
  "DNI":              e.numeroDi,
  "Cargo":            e.cargo,
  "Área":             e.departamento,
  "Sueldo":          `S/ ${e.sueldo.toFixed(2)}`,
  "Estado":           e.estado,
  "Contrato":         e.tipoContrato,
  "Fecha Ingreso":    e.fechaInicio,
}));

const REPORTE_NOMINA = NOMINAS_PERIODO.map(n => ({
  "ID Nómina":    n.idNomina,
  "Empleado":     n.empleadoNombre,
  "Período":     `${n.anio}-${n.mes}`,
  "Sueldo Base": `S/ ${n.sueldoBase.toFixed(2)}`,
  "Bonif.":      `S/ ${n.bonificaciones.toFixed(2)}`,
  "Desc.":       `S/ ${n.descuentos.toFixed(2)}`,
  "Neto":        `S/ ${n.sueldoNeto.toFixed(2)}`,
}));

const REPORTE_ASISTENCIA = EMPLEADOS.slice(0, 13).map((e, i) => ({
  "Empleado":            `${e.nombres} ${e.apellidos}`,
  "Área":                e.departamento,
  "Asistencias":         18 + (i % 4),
  "Tardanzas":           i % 3,
  "Justificaciones":     i % 2,
  "Horas Trabajadas":   `${148 + i * 2}h`,
}));

const REPORTE_SOLICITUDES = [...SOL_PENDIENTE, ...SOL_EN_REVISION, ...SOL_APROBADO, ...SOL_RECHAZADO].map(s => ({
  "ID":          s.idSolicitud,
  "Empleado":    s.nombreEmpleado,
  "Tipo":        s.tipo,
  "Inicio":      s.fechaInicio,
  "Fin":         s.fechaFin,
  "Días":        s.diasSolicitados,
  "Estado":      s.estado,
}));

const REPORTES = {
  empleados:   REPORTE_EMPLEADOS,
  nomina:      REPORTE_NOMINA,
  asistencia:  REPORTE_ASISTENCIA,
  solicitudes: REPORTE_SOLICITUDES,
};

/* ══════════════════════════════════════════════════════════════════
   HELPERS
══════════════════════════════════════════════════════════════════ */

const jsonOk = (data) => new Response(JSON.stringify(data), {
  status: 200,
  headers: { 'Content-Type': 'application/json' },
});

const ok = () => jsonOk({ success: true, message: 'OK (mock)' });

const paginate = (list, qp) => {
  const page = parseInt(qp.get('page') ?? 0);
  const size = parseInt(qp.get('size') ?? 20);
  return jsonOk({
    content: list.slice(page * size, (page + 1) * size),
    totalElements: list.length,
    totalPages: Math.max(1, Math.ceil(list.length / size)),
    number: page, size,
  });
};

const pathId = (path, segment) => {
  const m = path.match(new RegExp(`\\/${segment}\\/(\\d+)`));
  return m ? parseInt(m[1]) : null;
};

/* ══════════════════════════════════════════════════════════════════
   PATCH window.fetch
══════════════════════════════════════════════════════════════════ */

const _real = window.fetch.bind(window);

window.fetch = async (url, opts = {}) => {
  const urlStr = String(url);
  if (!urlStr.includes('localhost:8080')) return _real(url, opts);

  await new Promise(r => setTimeout(r, DELAY));

  const method = (opts.method || 'GET').toUpperCase();
  const urlObj = new URL(urlStr.startsWith('http') ? urlStr : `http://localhost:8080${urlStr}`);
  const path   = urlObj.pathname;
  const qp     = urlObj.searchParams;

  // ── EMPLEADOS ─────────────────────────────────────────────────
  if (path === '/api/v1/empleados/listar')
    return paginate(EMPLEADOS, qp);

  if (path === '/api/v1/empleados/buscar') {
    const q = (qp.get('filtro') || '').toLowerCase();
    const r = EMPLEADOS.filter(e =>
      `${e.nombres} ${e.apellidos}`.toLowerCase().includes(q) || e.numeroDi.includes(q)
    );
    return paginate(r, qp);
  }

  if (path === '/api/v1/empleados/buscar-avanzado') {
    let r = [...EMPLEADOS];
    const est = qp.get('estado'), car = qp.get('cargo');
    if (est) r = r.filter(e => e.estado === est);
    if (car) r = r.filter(e => e.cargo.toLowerCase().includes(car.toLowerCase()));
    return paginate(r, qp);
  }

  if (path === '/api/v1/empleados/registrar' && method === 'POST') return ok();
  if (/^\/api\/v1\/empleados\/\d+$/.test(path) && method === 'PUT') return ok();
  if (/\/api\/v1\/empleados\/\d+\/(desactivar|reactivar|sancion|ascenso|cambio-salarial)/.test(path)) return ok();

  // ── ASISTENCIA ────────────────────────────────────────────────
  if (path === '/api/v1/asistencia/manual') return ok();
  if (path === '/api/v1/asistencia/justificar' && method === 'POST') return ok();
  if (/\/api\/v1\/asistencia\/justificar\/\d+\/revisar/.test(path)) return ok();

  if (path === '/api/v1/asistencia/justificaciones/pendientes')
    return jsonOk(JUSTIF_PENDIENTES);

  if (/\/api\/v1\/asistencia\/justificaciones\/empleado\/\d+/.test(path)) {
    const id = pathId(path, 'empleado');
    return jsonOk(id === 1 ? JUSTIF_EMP1 : []);
  }

  // ── NÓMINA ────────────────────────────────────────────────────
  if (path === '/api/nomina/calcular' && method === 'POST')
    return jsonOk(NOMINAS_PERIODO);

  if (/^\/api\/nomina\/periodo\//.test(path))
    return jsonOk(NOMINAS_PERIODO);

  if (/^\/api\/nomina\/empleado\/\d+/.test(path)) {
    const id = pathId(path, 'empleado');
    return jsonOk(id === 1 ? NOMINAS_EMP1 : []);
  }

  if (/^\/api\/nomina\/\d+\/enviar/.test(path)) return ok();

  if (/^\/api\/nomina\/\d+\/comprobante/.test(path)) {
    const pdf = '%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R>>endobj\nxref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\ntrailer<</Root 1 0 R/Size 4>>\nstartxref\n183\n%%EOF';
    return new Response(new Blob([pdf], { type: 'application/pdf' }), {
      status: 200,
      headers: { 'Content-Type': 'application/pdf', 'Content-Disposition': 'attachment; filename="comprobante_mock.pdf"' },
    });
  }

  // ── SOLICITUDES ───────────────────────────────────────────────
  if (path === '/api/solicitud/registrar' && method === 'POST') {
    const nuevaId = 600 + SOL_NUEVAS.length + 1;
    const nuevaSol = {
      idSolicitud:   nuevaId,
      idEmpleado:    1,
      nombreEmpleado: "Ana García López",
      tipoSolicitud:  body?.tipoSolicitud ?? "OTROS",
      fechaInicio:    body?.fechaInicio   ?? null,
      fechaFin:       body?.fechaFin      ?? null,
      diasSolicitados: 1,
      motivo:         body?.motivo        ?? "",
      estado:         "PENDIENTE",
      observacionRrhh: null,
      respuesta:       null,
    };
    SOL_NUEVAS.push(nuevaSol);
    SOL_PENDIENTE.push(mkSol(nuevaId, 1, "Ana García López", nuevaSol.tipoSolicitud, "PENDIENTE",
      nuevaSol.fechaInicio, nuevaSol.fechaFin, 1, nuevaSol.motivo));
    return jsonOk(nuevaSol);
  }
  if (path === '/api/solicitud/revisar')    return ok();
  if (path === '/api/solicitud/decidir')    return ok();

  if (/^\/api\/solicitud\/estado\/(.+)/.test(path)) {
    const estado = path.match(/\/estado\/(.+)/)[1];
    return jsonOk(SOL_BY_ESTADO[estado] ?? []);
  }

  if (/^\/api\/solicitud\/empleado\/\d+/.test(path)) {
    const id = pathId(path, 'empleado');
    return jsonOk(id === 1 ? [...SOL_EMP1, ...SOL_NUEVAS] : []);
  }

  // ── REPORTES ──────────────────────────────────────────────────
  if (/^\/api\/reporte\/(\w+)/.test(path)) {
    const tipo = path.match(/\/reporte\/(\w+)/)[1];
    return jsonOk(REPORTES[tipo] ?? []);
  }

  // Auth (para si alguien llega al login en modo dev)
  if (path === '/api/v1/auth/login')      return jsonOk({ success: false, message: 'Usa /dev para ingresar en modo prueba.' });
  if (path === '/api/v1/auth/verify-2fa') return jsonOk({ success: false, message: 'Usa /dev para ingresar en modo prueba.' });

  console.warn('[MOCK] Sin ruta para:', method, path);
  return _real(url, opts);
};

console.info('%c[MOCK] Interceptor activo — todos los datos son de prueba', 'color:#7c3aed;font-weight:bold;');

} // fin if devMode

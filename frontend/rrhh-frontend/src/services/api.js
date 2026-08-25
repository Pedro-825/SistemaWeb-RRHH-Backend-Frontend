const BASE = import.meta.env.VITE_API_URL || '';

export const getUser       = () => JSON.parse(sessionStorage.getItem('user') || '{}');
export const getIdEmpleado = () => getUser()?.idEmpleado ?? null;
export const saveSession   = (token, rol, username, idEmpleado = null, requiereCambioPassword = false) =>
  sessionStorage.setItem('user', JSON.stringify({ rol, username, idEmpleado, requiereCambioPassword }));
export const clearSession  = () => sessionStorage.removeItem('user');

const CRED = 'include';

const STATUS_MESSAGES = {
  200: 'Operacion realizada correctamente',
  201: 'Recurso creado correctamente',
  204: 'Operacion realizada correctamente',
  400: 'Verifique los datos ingresados.',
  401: 'Sesion expirada. Inicie sesion nuevamente.',
  403: 'No tiene permisos para realizar esta accion.',
  404: 'No se encontro la informacion solicitada.',
  409: 'La informacion ya fue modificada. Actualice e intente nuevamente.',
  422: 'No se pudo procesar la informacion enviada.',
  500: 'No se pudo completar la operacion. Intente nuevamente.',
  502: 'El servidor no respondio correctamente.',
  503: 'Servicio no disponible temporalmente.',
};

const statusMessage = (status) => STATUS_MESSAGES[status] || `Respuesta HTTP ${status}`;

const extractMessage = (text) => {
  if (!text) return '';
  try {
    const parsed = JSON.parse(text);
    return parsed?.message || parsed?.mensaje || parsed?.error || parsed?.detail || text;
  } catch {
    return text;
  }
};

export const formatApiError = (status, detail = '') => {
  const cleanDetail = detail && !detail.startsWith('<') ? detail.trim() : '';
  if (cleanDetail) {
    if (/access denied/i.test(cleanDetail)) return 'No tiene permisos para consultar esta informacion.';
    return cleanDetail;
  }
  return statusMessage(status);
};

const json = (r) => {
  if (!r.ok) return r.text().then(text => {
    const msg = formatApiError(r.status, extractMessage(text));
    const shouldNotify = r.status === 401 || r.status === 0;
    if (shouldNotify) {
      window.dispatchEvent(new CustomEvent('api-error', { detail: { status: r.status, message: msg } }));
    }
    if (r.status === 401) {
      clearSession();
      if (window.location.pathname !== '/') sessionStorage.setItem('lastApiError', msg);
      if (window.location.pathname !== '/') window.location.href = '/';
    }
    const error = new Error(msg);
    error.status = r.status;
    throw error;
  });
  if (r.status === 204) return null;
  return r.json().catch(() => ({}));
};

const h = () => {
  return { 'Content-Type': 'application/json' };
};

const f = (url, opts = {}) =>
  fetch(url, { ...opts, credentials: CRED }).catch(() => {
    const msg = 'No se pudo conectar con el servidor. Verifique que el backend este disponible.';
    window.dispatchEvent(new CustomEvent('api-error', { detail: { status: 0, message: msg } }));
    throw new Error(msg);
  });

// ── AUTH ──────────────────────────────────────────────────────────────────────
export const solicitarRecuperacion = (identifier) =>
  f(`${BASE}/api/v1/auth/recovery`, {
    method: 'POST',
    headers: h(),
    body: JSON.stringify({ identifier }),
  }).then(json);

export const resetPassword = (token, nuevaContrasenia, confirmarContrasenia) =>
  f(`${BASE}/api/v1/auth/reset-password`, {
    method: 'POST',
    headers: h(),
    body: JSON.stringify({ token, nuevaContrasenia, confirmarContrasenia }),
  }).then(json);

export const setPassword = (nuevaContrasenia, confirmarContrasenia) =>
  f(`${BASE}/api/v1/auth/set-password`, {
    method: 'POST', headers: h(),
    body: JSON.stringify({ nuevaContrasenia, confirmarContrasenia }),
  }).then(json);

export const getMe = () =>
  f(`${BASE}/api/v1/auth/me`, { headers: { 'Content-Type': 'application/json' } }).then(r => {
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.json();
  });

export const login = (nombreUsuario, password) =>
  f(`${BASE}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombreUsuario, password }),
  }).then(json);

export const verify2FA = (username, code) =>
  f(`${BASE}/api/v1/auth/verify-2fa`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, code }),
  }).then(json);

export const logout = () =>
  f(`${BASE}/api/v1/auth/logout`, { method: 'POST', headers: h() }).then(() => {});

export const enable2FA = (username) =>
  f(`${BASE}/api/v1/auth/enable-2fa/${username}`, { headers: h() })
    .then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.blob(); });

// ── HORARIOS ──────────────────────────────────────────────────────────────────
export const getHorarios = () =>
  f(`${BASE}/api/v1/horarios`, { headers: h() }).then(json);

export const crearHorario = (dto) =>
  f(`${BASE}/api/v1/horarios`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const actualizarHorario = (id, dto) =>
  f(`${BASE}/api/v1/horarios/${id}`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const eliminarHorario = (id) =>
  f(`${BASE}/api/v1/horarios/${id}`, { method: 'DELETE', headers: h() })
    .then(r => r.status === 200 ? {} : r.json().catch(() => ({})));

export const asignarHorario = (dto) =>
  f(`${BASE}/api/v1/horarios/asignar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const getAsignacionesEmpleado = (idEmpleado) =>
  f(`${BASE}/api/v1/horarios/asignaciones/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const getAsignacionesActivas = () =>
  f(`${BASE}/api/v1/horarios/asignaciones/activas`, { headers: h() }).then(json);

export const getEmpleadosPorHorario = (idHorario) =>
  f(`${BASE}/api/v1/horarios/${idHorario}/empleados`, { headers: h() }).then(json);

// ── DEPARTAMENTOS ─────────────────────────────────────────────────────────────
export const getDepartamentos = () =>
  f(`${BASE}/api/v1/departamentos/listar`, { headers: h() }).then(json);

// ── EMPLEADOS ─────────────────────────────────────────────────────────────────
export const listarEmpleados = (page = 0, size = 20) =>
  f(`${BASE}/api/v1/empleados/listar?page=${page}&size=${size}`, { headers: h() }).then(json);

export const buscarEmpleados = (filtro, page = 0) =>
  f(`${BASE}/api/v1/empleados/buscar?filtro=${encodeURIComponent(filtro)}&page=${page}`, { headers: h() }).then(json);

export const buscarEmpleadosAvanzado = ({ estado, idDpto, cargo } = {}, page = 0) => {
  const p = new URLSearchParams({ page });
  if (estado) p.append('estado', estado);
  if (idDpto) p.append('idDpto', idDpto);
  if (cargo)  p.append('cargo', cargo);
  return f(`${BASE}/api/v1/empleados/buscar-avanzado?${p}`, { headers: h() }).then(json);
};

export const registrarEmpleado = (dto) =>
  f(`${BASE}/api/v1/empleados/registrar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const actualizarEmpleado = (id, dto) =>
  f(`${BASE}/api/v1/empleados/${id}`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const desactivarEmpleado = (id, motivo) =>
  f(`${BASE}/api/v1/empleados/${id}/desactivar`, {
    method: 'PUT', headers: h(), body: JSON.stringify({ motivo }),
  }).then(json);

export const reactivarEmpleado = (id, motivo) =>
  f(`${BASE}/api/v1/empleados/${id}/reactivar`, {
    method: 'PUT', headers: h(), body: JSON.stringify({ motivo }),
  }).then(json);

export const registrarSancion = (id, dto) =>
  f(`${BASE}/api/v1/empleados/${id}/sancion`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const registrarAscenso = (id, dto) =>
  f(`${BASE}/api/v1/empleados/${id}/ascenso`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const cambioSalarial = (id, dto) =>
  f(`${BASE}/api/v1/empleados/${id}/cambio-salarial`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const getHistorialEmpleado = (idEmpleado) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/historial`, { headers: h() }).then(json);

export const getBeneficiosEmpleado = (idEmpleado) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/beneficios`, { headers: h() }).then(json);

export const guardarPerfilBeneficios = (idEmpleado, dto) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/beneficios`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const agregarFamiliarEmpleado = (idEmpleado, dto) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/beneficios/familiares`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const actualizarFamiliarEmpleado = (idEmpleado, idFamiliar, dto) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/beneficios/familiares/${idFamiliar}`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const eliminarFamiliarEmpleado = (idEmpleado, idFamiliar) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/beneficios/familiares/${idFamiliar}`, {
    method: 'DELETE', headers: h(),
  }).then(r => (r.status === 204 ? true : json(r)));

// ── ASISTENCIA ────────────────────────────────────────────────────────────────
export const registrarManual = (dto) =>
  f(`${BASE}/api/v1/asistencia/manual`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

export const getAsistenciaHoy = (idEmpleado) =>
  f(`${BASE}/api/v1/asistencia/hoy/${idEmpleado}`, { headers: h() }).then(r => {
    if (!r.ok) return json(r);
    if (r.status === 204) return null;
    return r.json();
  });

export const getMiAsistenciaHoy = () =>
  f(`${BASE}/api/v1/asistencia/mi/hoy`, { headers: h() }).then(r => {
    if (!r.ok) return json(r);
    if (r.status === 204) return null;
    return r.json();
  });

export const listarAsistenciaEmpleado = (idEmpleado) =>
  f(`${BASE}/api/v1/asistencia/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const listarMiAsistencia = () =>
  f(`${BASE}/api/v1/asistencia/mi/empleado`, { headers: h() }).then(json);

export const listarJustificacionesPendientes = () =>
  f(`${BASE}/api/v1/asistencia/justificaciones/pendientes`, { headers: h() }).then(json);

export const listarTodasJustificaciones = () =>
  f(`${BASE}/api/v1/asistencia/justificaciones/todas`, { headers: h() }).then(json);

export const listarCorreccionesRegistradas = () =>
  f(`${BASE}/api/v1/asistencia/correcciones-registradas`, { headers: h() }).then(json);

export const obtenerPlanRecuperacion = (idJustificacion) =>
  f(`${BASE}/api/v1/asistencia/plan-recuperacion/${idJustificacion}`, { headers: h() }).then(json);

export const listarJustificacionesEmpleado = (idEmpleado) =>
  f(`${BASE}/api/v1/asistencia/justificaciones/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const listarMisJustificaciones = () =>
  f(`${BASE}/api/v1/asistencia/mi/justificaciones`, { headers: h() }).then(json);

export const getHorarioEmpleado = (idEmpleado) =>
  f(`${BASE}/api/v1/asistencia/horario/${idEmpleado}`, { headers: h() }).then(json);

export const getMiHorario = () =>
  f(`${BASE}/api/v1/asistencia/mi/horario`, { headers: h() }).then(json);

export const uploadEvidencia = (file) => {
  const fd = new FormData();
  fd.append("file", file);
  return f(`${BASE}/api/v1/asistencia/upload-evidencia`, {
    method: "POST",
    body: fd,
  }).then(json);
};

export const registrarJustificacion = (dto) =>
  f(`${BASE}/api/v1/asistencia/justificar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

// Backend espera "APROBADA" / "RECHAZADA" (femenino)
export const revisarJustificacion = (id, decision, comentario = '') =>
  f(`${BASE}/api/v1/asistencia/justificar/${id}/revisar`, {
    method: 'PUT', headers: h(), body: JSON.stringify({ decision, comentario }),
  }).then(json);

// ── NÓMINA ────────────────────────────────────────────────────────────────────
// ECU-04: calcular nómina por mes y año
export const calcularNomina = (mes, anio) => {
  const mesStr = String(mes).padStart(2, '0');
  const anioStr = String(anio);
  const ultimoDia = new Date(Number(anioStr), Number(mesStr), 0).getDate();
  const fechaInicio = `${anioStr}-${mesStr}-01`;
  const fechaFin = `${anioStr}-${mesStr}-${String(ultimoDia).padStart(2, '0')}`;
  return f(`${BASE}/api/nomina/calcular`, {
    method: 'POST', headers: h(), body: JSON.stringify({ fechaInicio, fechaFin }),
  }).then(json);
};

// ECU-04: cargar nóminas de un período ya calculado
export const getNominasPorPeriodo = (mes, anio) =>
  f(`${BASE}/api/nomina/periodo/${anio}-${mes}`, { headers: h() }).then(json);

export const getNominasPorEmpleado = (idEmpleado) =>
  f(`${BASE}/api/nomina/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const enviarComprobante = (id) =>
  f(`${BASE}/api/nomina/${id}/enviar`, {
    method: 'POST', headers: h(),
  }).then(json);

export const enviarComprobantesPeriodo = (periodo) =>
  f(`${BASE}/api/nomina/periodo/${periodo}/enviar`, {
    method: 'POST', headers: h(),
  }).then(json);

export const ajustarNomina = (id, dto) =>
  f(`${BASE}/api/nomina/${id}/ajustar`, {
    method: 'PUT', headers: h(), body: JSON.stringify(dto),
  }).then(json);

// Returns a Blob for PDF download
export const descargarComprobante = (id) =>
  f(`${BASE}/api/nomina/${id}/comprobante`, { headers: h() }).then(r => {
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return r.blob();
  });

// ── GALERÍA ───────────────────────────────────────────────────────────────────
export const listarGaleria = (idEmpleado) =>
  f(`${BASE}/api/v1/galeria/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const subirFotoGaleria = (idEmpleado, file) => {
  const fd = new FormData();
  fd.append("file", file);
  fd.append("idEmpleado", idEmpleado);
  return f(`${BASE}/api/v1/galeria/subir`, { method: "POST", body: fd }).then(json);
};

export const eliminarFotoGaleria = (id) =>
  f(`${BASE}/api/v1/galeria/${id}`, { method: "DELETE", headers: h() }).then(json);

// ── APP MÓVIL ─────────────────────────────────────────────────────────────────
export const enviarLinkApp = (idEmpleado) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/enviar-app`, {
    method: 'POST', headers: h(),
  }).then(json);

export const confirmarAppInstalada = (idEmpleado) =>
  f(`${BASE}/api/v1/empleados/${idEmpleado}/confirmar-app`, {
    method: 'POST', headers: h(),
  }).then(json);

// ── SOLICITUDES ───────────────────────────────────────────────────────────────
export const registrarSolicitud = (dto) =>
  f(`${BASE}/api/solicitud/registrar`, {
    method: 'POST', headers: h(), body: JSON.stringify(dto),
  }).then(json);

// ECU-05: RRHH revisa y envía a Gerencia (decision: "APROBAR" | "DEVOLVER")
export const revisarSolicitudRRHH = (idSolicitud, { decision, comentario } = {}) =>
  f(`${BASE}/api/solicitud/revisar`, {
    method: 'PUT', headers: h(),
    body: JSON.stringify({
      idSolicitud,
      aprobado: decision === 'APROBAR',
      observacionRrhh: comentario,
    }),
  }).then(json);

// ECU-05: Gerencia aprueba o rechaza (decision: "APROBAR" | "RECHAZAR")
export const decidirSolicitudGerencia = (idSolicitud, { decision, comentario } = {}) =>
  f(`${BASE}/api/solicitud/decidir`, {
    method: 'PUT', headers: h(),
    body: JSON.stringify({
      idSolicitud,
      aprobado: decision === 'APROBAR',
      respuesta: comentario,
    }),
  }).then(json);

export const getSolicitudesPorEstado = (estado) =>
  f(`${BASE}/api/solicitud/estado/${estado}`, { headers: h() }).then(json);

export const getSolicitudesPorEmpleado = (idEmpleado) =>
  f(`${BASE}/api/solicitud/empleado/${idEmpleado}`, { headers: h() }).then(json);

export const getMiSaldoVacaciones = () =>
  f(`${BASE}/api/v1/solicitudes/mi/saldo-vacaciones`, { headers: h() }).then(json);

// ── REPORTES ──────────────────────────────────────────────────────────────────
// ECU-06: params = { tipo, mes, anio, fechaInicio, fechaFin, estado, ... }
// Devuelve el JSON de vista previa; el formato (PDF/XLSX/CSV) se aplica en el
// cliente al exportar, no aqui.
export const generarReporte = (params) => {
  const { tipo, ...filtros } = params;
  return f(`${BASE}/api/reporte/${tipo}`, {
    method: 'POST', headers: h(), body: JSON.stringify(filtros),
  }).then(json);
};

// Para el APK de produccion usamos Azure por defecto.
// En desarrollo puedes sobrescribirlo con EXPO_PUBLIC_API_URL.
const AZURE_BASE_URL = 'https://rrhh-san-gabriel-api-hahbaubnauakctgs.spaincentral-01.azurewebsites.net';

const normalizarUrl = (url) => url?.trim().replace(/\/+$/, '');

const esUrlPrivada = (url) => {
  try {
    const { hostname } = new URL(url);
    return hostname === 'localhost'
      || hostname === '127.0.0.1'
      || hostname === '10.0.2.2'
      || hostname.startsWith('10.')
      || hostname.startsWith('192.168.')
      || /^172\.(1[6-9]|2\d|3[01])\./.test(hostname);
  } catch {
    return false;
  }
};

const obtenerBaseUrl = () => {
  const envUrl = normalizarUrl(process.env.EXPO_PUBLIC_API_URL);
  if (!envUrl) return AZURE_BASE_URL;
  if (!__DEV__ && esUrlPrivada(envUrl)) return AZURE_BASE_URL;
  return envUrl;
};

const BASE_URL = obtenerBaseUrl();

const STATUS_MESSAGES = {
  400: 'Solicitud invalida. Verifica los datos enviados.',
  401: 'No autorizado. Vuelve a iniciar el registro.',
  403: 'Acceso denegado para este dispositivo.',
  404: 'No se encontro el recurso solicitado.',
  409: 'Ya existe un registro en conflicto.',
  500: 'El servidor no pudo completar la solicitud.',
  503: 'Servicio no disponible temporalmente.',
};

const mensajeHttp = (status) => STATUS_MESSAGES[status] || `Error HTTP ${status}`;

const fetchSeguro = async (url, options) => {
  try {
    return await fetch(url, options);
  } catch {
    throw new Error('No se pudo conectar con el servidor. Verifica tu conexion e intenta nuevamente.');
  }
};

const leerRespuesta = async (res) => {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
};

const mensajeError = (data, fallback) => {
  if (!data) return fallback;
  if (typeof data === 'string') return data;
  return data.mensaje || data.message || fallback;
};

export const getEstadoAsistenciaHoy = async (idEmpleado, deviceToken) => {
  const res = await fetchSeguro(
    `${BASE_URL}/api/v1/asistencia/biometrico/estado/${idEmpleado}?deviceToken=${encodeURIComponent(deviceToken)}`
  );
  if (!res.ok) throw new Error(mensajeHttp(res.status));
  return res.json();
};

export const buscarEmpleadoPorDni = async (numeroDi) => {
  const res = await fetchSeguro(`${BASE_URL}/api/dispositivos/dni/${numeroDi}`);
  if (res.status === 404) throw new Error('DNI no encontrado. Verifica tu numero de documento.');
  if (res.status === 400) throw new Error('Empleado inactivo o desvinculado.');
  if (!res.ok) throw new Error(mensajeHttp(res.status));
  return res.json();
};

export const enviarOtp = async (numeroDi) => {
  const res = await fetchSeguro(`${BASE_URL}/api/dispositivos/enviar-otp`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ numeroDi }),
  });
  const data = await leerRespuesta(res);
  if (!res.ok) throw new Error(mensajeError(data, mensajeHttp(res.status)));
  return data;
};

export const registrarDispositivo = async (numeroDi, deviceToken, sessionId, codigo) => {
  const res = await fetchSeguro(`${BASE_URL}/api/dispositivos/registrar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ numeroDi, deviceToken, sessionId, codigo }),
  });
  const data = await leerRespuesta(res);
  if (!res.ok) throw new Error(mensajeError(data, mensajeHttp(res.status)));
  return data;
};

export const registrarAsistenciaBiometrica = async (idEmpleado, deviceToken, tipo) => {
  const res = await fetchSeguro(`${BASE_URL}/api/v1/asistencia/biometrico`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idEmpleado, deviceToken, tipo }),
  });
  const data = await leerRespuesta(res);
  if (!res.ok) throw new Error(mensajeError(data, mensajeHttp(res.status)));
  return data;
};

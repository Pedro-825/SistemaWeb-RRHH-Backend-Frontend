import { useState, useEffect, useCallback } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert, ScrollView,
} from 'react-native';
import { registrarAsistenciaBiometrica, getEstadoAsistenciaHoy } from '../services/api';

const getSecureStore = () => require('expo-secure-store');
const getLocalAuthentication = () => require('expo-local-authentication');

const ESTADO_INICIAL = {
  tieneEntrada: false,
  tieneSalida: false,
  horaEntrada: null,
  horaSalida: null,
  estado: null,
};

export default function AsistenciaScreen({ onDesvincular }) {
  const [nombre, setNombre] = useState('Empleado');
  const [hora, setHora] = useState('');
  const [cargando, setCargando] = useState(false);
  const [cargandoEstado, setCargandoEstado] = useState(true);
  const [resultado, setResultado] = useState(null);
  const [authDisponible, setAuthDisponible] = useState(false);
  const [authMensaje, setAuthMensaje] = useState('Verificando seguridad del dispositivo...');
  const [estadoHoy, setEstadoHoy] = useState(ESTADO_INICIAL);

  const cargarEstado = useCallback(async () => {
    setCargandoEstado(true);
    try {
      const SecureStore = getSecureStore();
      const idEmpleado = await SecureStore.getItemAsync('idEmpleado');
      const deviceToken = await SecureStore.getItemAsync('deviceToken');
      if (!idEmpleado || !deviceToken) {
        setEstadoHoy(ESTADO_INICIAL);
        return;
      }
      const data = await getEstadoAsistenciaHoy(idEmpleado, deviceToken);
      setEstadoHoy({ ...ESTADO_INICIAL, ...(data || {}) });
    } catch (e) {
      setResultado({ exito: false, error: e.message || 'No se pudo cargar la asistencia de hoy.' });
    } finally {
      setCargandoEstado(false);
    }
  }, []);

  useEffect(() => {
    const tick = () => setHora(new Date().toLocaleTimeString('es-PE', {
      hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false, timeZone: 'America/Lima',
    }));
    tick();
    const t = setInterval(tick, 1000);

    getSecureStore().getItemAsync('nombreEmpleado')
      .then(n => setNombre(n || 'Empleado'))
      .catch(() => setNombre('Empleado'));

    const verificarAutenticacionLocal = async () => {
      try {
        const LocalAuthentication = getLocalAuthentication();
        const tieneHardware = await LocalAuthentication.hasHardwareAsync();
        const tieneBiometria = await LocalAuthentication.isEnrolledAsync();
        // Acepta cualquier biometria segura del telefono (huella, rostro o iris),
        // no solo huella: muchos Android usan reconocimiento facial como principal.
        const tiposSoportados = await LocalAuthentication.supportedAuthenticationTypesAsync();
        const tipo = LocalAuthentication.AuthenticationType ?? {};
        const tiposAceptados = [tipo.FINGERPRINT ?? 1, tipo.FACIAL_RECOGNITION ?? 2, tipo.IRIS ?? 3];
        const tieneBiometriaSegura = tiposSoportados.some(t => tiposAceptados.includes(t));
        const disponible = tieneHardware && tieneBiometria && tieneBiometriaSegura;

        setAuthDisponible(disponible);
        setAuthMensaje(disponible
          ? 'Listo para validar tu identidad.'
          : 'Configura una huella digital o reconocimiento facial en este telefono para marcar asistencia.');
      } catch {
        setAuthDisponible(false);
        setAuthMensaje('No se pudo verificar la biometria del dispositivo.');
      }
    };

    verificarAutenticacionLocal();
    cargarEstado();
    return () => clearInterval(t);
  }, [cargarEstado]);

  const marcar = async (tipo) => {
    if (estadoHoy.estado === 'INASISTENCIA') {
      Alert.alert('Inasistencia registrada', 'Ya pasó tu horario de salida sin marcar entrada. Ingresa a la plataforma web para justificarla con evidencia.');
      return;
    }
    if (tipo === 'ENTRADA' && estadoHoy.tieneEntrada) {
      Alert.alert('Ya registrado', 'Ya tienes tu entrada registrada hoy.');
      return;
    }
    if (tipo === 'SALIDA' && !estadoHoy.tieneEntrada) {
      Alert.alert('Sin entrada', 'Primero debes registrar tu entrada.');
      return;
    }
    if (tipo === 'SALIDA' && estadoHoy.tieneSalida) {
      Alert.alert('Ya registrado', 'Ya tienes tu salida registrada hoy.');
      return;
    }
    if (estadoHoy.tieneEntrada && estadoHoy.tieneSalida) {
      Alert.alert('Dia completado', 'Tu asistencia de hoy ya esta completa.');
      return;
    }
      if (!authDisponible) {
      Alert.alert('Biometria requerida', 'Configura una huella digital o reconocimiento facial en este telefono para marcar asistencia.');
      return;
    }

    setCargando(true);
    setResultado(null);
    try {
      const LocalAuthentication = getLocalAuthentication();
      const auth = await LocalAuthentication.authenticateAsync({
        promptMessage: `Valida tu identidad para registrar ${tipo === 'ENTRADA' ? 'entrada' : 'salida'}`,
        cancelLabel: 'Cancelar',
        // Si falla el sensor biometrico, se permite el PIN/patron del propio telefono
        // como respaldo -- el telefono ya esta vinculado a este empleado via OTP, asi
        // que no queremos bloquear el marcado por un sensor con fallas puntuales.
        disableDeviceFallback: false,
        biometricsSecurityLevel: 'strong',
      });
      if (!auth.success) return;

      const SecureStore = getSecureStore();
      const deviceToken = await SecureStore.getItemAsync('deviceToken');
      const idEmpleado = await SecureStore.getItemAsync('idEmpleado');
      if (!deviceToken || !idEmpleado) {
        throw new Error('Dispositivo no vinculado. Registra nuevamente el celular.');
      }

      const resp = await registrarAsistenciaBiometrica(Number(idEmpleado), deviceToken, tipo);
      const horaReg = new Date().toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false, timeZone: 'America/Lima' });
      setResultado({
        tipo: resp.tipoRegistro || tipo,
        hora: horaReg,
        exito: true,
        mensaje: resp.message || 'Registro guardado correctamente.',
      });
      await cargarEstado();
    } catch (e) {
      setResultado({ tipo, hora: '', exito: false, error: e.message || 'No se pudo registrar la asistencia.' });
    } finally {
      setCargando(false);
    }
  };

  const desvincular = () => {
    Alert.alert(
      'Desvincular dispositivo',
      'Se borrara el registro local para vincular otro empleado o repetir la demo.',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Desvincular',
          style: 'destructive',
          onPress: async () => {
            const SecureStore = getSecureStore();
            await SecureStore.deleteItemAsync('deviceToken');
            await SecureStore.deleteItemAsync('idEmpleado');
            await SecureStore.deleteItemAsync('nombreEmpleado');
            onDesvincular?.();
          },
        },
      ],
    );
  };

  const esInasistencia = estadoHoy.estado === 'INASISTENCIA';
  const entradaHabilitada = !estadoHoy.tieneEntrada && !estadoHoy.tieneSalida && !esInasistencia;
  const salidaHabilitada = estadoHoy.tieneEntrada && !estadoHoy.tieneSalida && !esInasistencia;
  const diaCompleto = estadoHoy.tieneEntrada && estadoHoy.tieneSalida;

  return (
    <ScrollView contentContainerStyle={s.container}>
      <View style={s.header}>
        <Text style={s.headerLogo}>HSG</Text>
        <Text style={s.headerTitle}>Hospital San Gabriel</Text>
        <Text style={s.headerSub}>Sistema de Asistencia</Text>
      </View>

      <View style={s.card}>
        <Text style={s.cardNombre}>{nombre}</Text>
        <Text style={s.cardHora}>{hora}</Text>
        <Text style={s.cardFecha}>
          {new Date().toLocaleDateString('es-PE', { weekday: 'long', day: 'numeric', month: 'long', timeZone: 'America/Lima' })}
        </Text>

        {!cargandoEstado && (
          <View style={s.resumenRow}>
            <View style={s.resumenItem}>
              <Text style={s.resumenLabel}>Entrada</Text>
              <Text style={[s.resumenVal, estadoHoy.horaEntrada ? s.ok : s.pendiente]}>
                {estadoHoy.horaEntrada ?? '--:--'}
              </Text>
            </View>
            <View style={s.resumenItem}>
              <Text style={s.resumenLabel}>Salida</Text>
              <Text style={[s.resumenVal, estadoHoy.horaSalida ? s.ok : s.pendiente]}>
                {estadoHoy.horaSalida ?? '--:--'}
              </Text>
            </View>
          </View>
        )}
      </View>

      {diaCompleto && (
        <View style={s.bannerCompleto}>
          <Text style={s.bannerText}>Asistencia completada hoy</Text>
        </View>
      )}

      {esInasistencia && (
        <View style={s.bannerInasistencia}>
          <Text style={s.bannerInasistenciaTitulo}>Ya pasó tu horario de salida</Text>
          <Text style={s.bannerInasistenciaTexto}>
            Tu asistencia de hoy quedó marcada como inasistencia. Ingresa a la
            plataforma web para justificarla con evidencia.
          </Text>
        </View>
      )}

      {resultado && (
        <View style={[s.resultado, resultado.exito ? s.resultadoOk : s.resultadoErr]}>
          <Text style={s.resultadoText}>
            {resultado.exito
              ? `${resultado.tipo === 'ENTRADA' ? 'Entrada' : 'Salida'} registrada a las ${resultado.hora}`
              : resultado.error || 'No se pudo registrar'}
          </Text>
        </View>
      )}

      {cargando ? (
        <ActivityIndicator size="large" color="#2563eb" style={{ marginTop: 32 }} />
      ) : cargandoEstado ? (
        <ActivityIndicator size="small" color="#94a3b8" style={{ marginTop: 32 }} />
      ) : (
        <View style={s.botones}>
          <TouchableOpacity
            style={[s.btn, s.btnEntrada, !entradaHabilitada && s.btnDisabled]}
            onPress={() => marcar('ENTRADA')}
            disabled={!entradaHabilitada}
          >
            <Text style={[s.btnText, !entradaHabilitada && s.btnTextDisabled]}>
              {estadoHoy.tieneEntrada
                ? `Entrada\n${estadoHoy.horaEntrada}`
                : esInasistencia
                  ? 'Bloqueado\n(inasistencia)'
                  : 'Registrar\nEntrada'}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[s.btn, s.btnSalida, !salidaHabilitada && s.btnDisabled]}
            onPress={() => marcar('SALIDA')}
            disabled={!salidaHabilitada}
          >
            <Text style={[s.btnText, !salidaHabilitada && s.btnTextDisabled]}>
              {estadoHoy.tieneSalida
                ? `Salida\n${estadoHoy.horaSalida}`
                : esInasistencia
                  ? 'Bloqueado\n(inasistencia)'
                  : !estadoHoy.tieneEntrada
                    ? 'Bloqueado\n(sin entrada)'
                    : 'Registrar\nSalida'}
            </Text>
          </TouchableOpacity>
        </View>
      )}

      <Text style={s.hint}>{authMensaje}</Text>

      <TouchableOpacity style={s.linkButton} onPress={desvincular} disabled={cargando}>
        <Text style={s.linkButtonText}>Desvincular dispositivo</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const s = StyleSheet.create({
  container: { flexGrow: 1, backgroundColor: '#f8fafc', padding: 24, paddingBottom: 36 },
  header: { alignItems: 'center', marginTop: 32, marginBottom: 24 },
  headerLogo: {
    width: 58,
    height: 58,
    borderRadius: 16,
    backgroundColor: '#2563eb',
    color: '#fff',
    textAlign: 'center',
    textAlignVertical: 'center',
    fontSize: 20,
    fontWeight: '900',
    marginBottom: 10,
  },
  headerTitle: { fontSize: 20, fontWeight: '700', color: '#1e293b', textAlign: 'center' },
  headerSub: { fontSize: 13, color: '#64748b', marginTop: 2, textAlign: 'center' },
  card: {
    backgroundColor: '#fff', borderRadius: 16, padding: 24,
    alignItems: 'center', marginBottom: 20,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08, shadowRadius: 8, elevation: 3,
  },
  cardNombre: { fontSize: 18, fontWeight: '700', color: '#1e293b', marginBottom: 8, textAlign: 'center' },
  cardHora: { fontSize: 38, fontWeight: '800', color: '#2563eb', fontVariant: ['tabular-nums'] },
  cardFecha: { fontSize: 13, color: '#64748b', marginTop: 4, textTransform: 'capitalize', textAlign: 'center' },
  resumenRow: { flexDirection: 'row', gap: 24, marginTop: 16 },
  resumenItem: { alignItems: 'center' },
  resumenLabel: { fontSize: 11, color: '#94a3b8', textTransform: 'uppercase', marginBottom: 2 },
  resumenVal: { fontSize: 16, fontWeight: '700' },
  ok: { color: '#16a34a' },
  pendiente: { color: '#cbd5e1' },
  bannerCompleto: {
    alignItems: 'center', justifyContent: 'center',
    backgroundColor: '#dcfce7', borderRadius: 12, padding: 12, marginBottom: 16,
  },
  bannerText: { fontSize: 14, fontWeight: '700', color: '#15803d' },
  bannerInasistencia: {
    backgroundColor: '#fee2e2', borderRadius: 12, padding: 14, marginBottom: 16,
  },
  bannerInasistenciaTitulo: { fontSize: 14, fontWeight: '700', color: '#dc2626', marginBottom: 4 },
  bannerInasistenciaTexto: { fontSize: 12, color: '#991b1b', lineHeight: 18 },
  resultado: { borderRadius: 12, padding: 16, marginBottom: 16 },
  resultadoOk: { backgroundColor: '#dcfce7' },
  resultadoErr: { backgroundColor: '#fee2e2' },
  resultadoText: { fontSize: 14, fontWeight: '600', color: '#1e293b', lineHeight: 20, textAlign: 'center' },
  botones: { flexDirection: 'row', gap: 16, marginTop: 8 },
  btn: {
    flex: 1, borderRadius: 16, padding: 24, alignItems: 'center',
    shadowColor: '#000', shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15, shadowRadius: 8, elevation: 4,
  },
  btnEntrada: { backgroundColor: '#16a34a' },
  btnSalida: { backgroundColor: '#dc2626' },
  btnDisabled: { backgroundColor: '#e2e8f0', shadowOpacity: 0, elevation: 0 },
  btnText: { color: '#fff', fontSize: 15, fontWeight: '700', textAlign: 'center', lineHeight: 22 },
  btnTextDisabled: { color: '#94a3b8' },
  hint: { textAlign: 'center', color: '#64748b', fontSize: 12, marginTop: 16, lineHeight: 18 },
  linkButton: { alignSelf: 'center', marginTop: 16, paddingVertical: 8, paddingHorizontal: 12 },
  linkButtonText: { color: '#64748b', fontSize: 12, fontWeight: '600' },
});

import { useState, useEffect, useRef } from 'react';
import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, ActivityIndicator, Alert,
} from 'react-native';
import { enviarOtp, registrarDispositivo, buscarEmpleadoPorDni } from '../services/api';

const getSecureStore = () => require('expo-secure-store');

const crearDeviceToken = (dni) => {
  const base = `${dni}-${Date.now()}-${Math.random()}-${Math.random()}`;
  return `hsg-${base.replace(/[^a-zA-Z0-9]/g, '')}`;
};

export default function RegistroScreen({ onRegistrado }) {
  const [step, setStep] = useState('dni');
  const [dni, setDni] = useState('');
  const [codigo, setCodigo] = useState('');
  const [sessionId, setSessionId] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const timerRef = useRef(null);

  useEffect(() => {
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, []);

  const iniciarCooldown = () => {
    setCooldown(15);
    timerRef.current = setInterval(() => {
      setCooldown(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          timerRef.current = null;
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const handleSolicitarCodigo = async () => {
    if (!dni.trim()) {
      Alert.alert('Campo requerido', 'Ingresa tu numero de DNI.');
      return;
    }
    if (!/^\d{8}$/.test(dni.trim())) {
      Alert.alert('DNI invalido', 'El DNI debe tener exactamente 8 digitos.');
      return;
    }
    setCargando(true);
    try {
      await buscarEmpleadoPorDni(dni.trim());
      const resp = await enviarOtp(dni.trim());
      setSessionId(resp.sessionId);
      setCodigo('');
      setStep('otp');
      iniciarCooldown();
    } catch (e) {
      Alert.alert('Error', e.message || 'No se pudo enviar el codigo.');
    } finally {
      setCargando(false);
    }
  };

  const handleReenviarCodigo = async () => {
    if (cooldown > 0) return;
    setCargando(true);
    try {
      const resp = await enviarOtp(dni.trim());
      setSessionId(resp.sessionId);
      setCodigo('');
      iniciarCooldown();
      Alert.alert('Codigo reenviado', 'Revisa tu correo personal.');
    } catch (e) {
      Alert.alert('Error', e.message || 'No se pudo reenviar el codigo.');
    } finally {
      setCargando(false);
    }
  };

  const handleVincular = async () => {
    if (codigo.trim().length !== 6) {
      Alert.alert('Codigo incompleto', 'Ingresa los 6 digitos del codigo.');
      return;
    }
    setCargando(true);
    try {
      const SecureStore = getSecureStore();
      const deviceToken = crearDeviceToken(dni.trim());
      const resp = await registrarDispositivo(dni.trim(), deviceToken, sessionId, codigo.trim());

      await SecureStore.setItemAsync('deviceToken', deviceToken);
      await SecureStore.setItemAsync('idEmpleado', String(resp.idEmpleado));
      await SecureStore.setItemAsync('nombreEmpleado', `${resp.nombres} ${resp.apellidos}`);

      onRegistrado();
    } catch (e) {
      Alert.alert('Error', e.message || 'No se pudo vincular el dispositivo.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <View style={s.container}>
      <Text style={s.logo}>HSG</Text>
      <Text style={s.titulo}>Hospital San Gabriel</Text>
      <Text style={s.subtitulo}>Registro de dispositivo</Text>

      {step === 'dni' ? (
        <>
          <Text style={s.desc}>
            Ingresa tu DNI para recibir un codigo de verificacion{'\n'}
            en tu correo personal.
          </Text>

          <TextInput
            style={s.input}
            placeholder="Numero de DNI (8 digitos)"
            placeholderTextColor="#94a3b8"
            keyboardType="numeric"
            maxLength={8}
            value={dni}
            onChangeText={setDni}
          />

          <TouchableOpacity style={s.btn} onPress={handleSolicitarCodigo} disabled={cargando}>
            {cargando
              ? <ActivityIndicator color="#fff" />
              : <Text style={s.btnText}>Solicitar codigo</Text>
            }
          </TouchableOpacity>
        </>
      ) : (
        <>
          <Text style={s.desc}>
            Ingresa el codigo de 6 digitos que enviamos a tu correo personal.
          </Text>

          <View style={s.dniBadge}>
            <Text style={s.dniBadgeText}>DNI: {dni}</Text>
          </View>

          <TextInput
            style={[s.input, s.inputCode]}
            placeholder="Codigo"
            placeholderTextColor="#94a3b8"
            keyboardType="numeric"
            maxLength={6}
            value={codigo}
            onChangeText={setCodigo}
          />

          <TouchableOpacity style={s.btn} onPress={handleVincular} disabled={cargando || codigo.trim().length !== 6}>
            {cargando
              ? <ActivityIndicator color="#fff" />
              : <Text style={s.btnText}>Vincular dispositivo</Text>
            }
          </TouchableOpacity>

          <TouchableOpacity
            style={[s.btnReenviar, cooldown > 0 && s.btnReenviarDisabled]}
            onPress={handleReenviarCodigo}
            disabled={cargando || cooldown > 0}
          >
            <Text style={[s.btnReenviarText, cooldown > 0 && s.btnReenviarTextDisabled]}>
              {cooldown > 0 ? `Reenviar codigo (${cooldown}s)` : 'Reenviar codigo'}
            </Text>
          </TouchableOpacity>
        </>
      )}
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f0f9ff', alignItems: 'center', justifyContent: 'center', padding: 32 },
  logo: {
    width: 72,
    height: 72,
    borderRadius: 20,
    backgroundColor: '#2563eb',
    color: '#fff',
    textAlign: 'center',
    textAlignVertical: 'center',
    fontSize: 24,
    fontWeight: '900',
    marginBottom: 12,
  },
  titulo: { fontSize: 22, fontWeight: '700', color: '#1e293b', marginBottom: 4, textAlign: 'center' },
  subtitulo: { fontSize: 15, color: '#2563eb', fontWeight: '600', marginBottom: 16, textAlign: 'center' },
  desc: { fontSize: 13, color: '#64748b', textAlign: 'center', marginBottom: 28, lineHeight: 20 },
  input: {
    width: '100%', borderWidth: 1.5, borderColor: '#cbd5e1', borderRadius: 12,
    padding: 14, fontSize: 16, color: '#1e293b', backgroundColor: '#fff',
    marginBottom: 16,
  },
  inputCode: { fontSize: 24, fontWeight: '700', textAlign: 'center', letterSpacing: 8 },
  btn: {
    width: '100%', backgroundColor: '#2563eb', borderRadius: 12,
    padding: 16, alignItems: 'center',
    shadowColor: '#2563eb', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8,
    elevation: 4,
  },
  btnText: { color: '#fff', fontSize: 16, fontWeight: '700' },
  dniBadge: {
    backgroundColor: '#e0f2fe', borderRadius: 8,
    paddingVertical: 6, paddingHorizontal: 16, marginBottom: 20,
  },
  dniBadgeText: { color: '#0369a1', fontSize: 14, fontWeight: '600' },
  btnReenviar: {
    marginTop: 16, paddingVertical: 10, paddingHorizontal: 20,
  },
  btnReenviarDisabled: { opacity: 0.5 },
  btnReenviarText: { color: '#2563eb', fontSize: 14, fontWeight: '600', textAlign: 'center' },
  btnReenviarTextDisabled: { color: '#94a3b8' },
});

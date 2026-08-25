import { Component, useEffect, useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import RegistroScreen  from './src/screens/RegistroScreen';
import AsistenciaScreen from './src/screens/AsistenciaScreen';

const getSecureStore = () => require('expo-secure-store');

class AppErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  render() {
    if (!this.state.hasError) return this.props.children;
    return (
      <View style={s.crashContainer}>
        <Text style={s.crashTitle}>No se pudo abrir la app</Text>
        <Text style={s.crashText}>
          Reinicia la pantalla e intenta nuevamente. Si el problema continua,
          vuelve a vincular el dispositivo.
        </Text>
        <Text style={s.crashDetail}>
          {this.state.error?.message || 'Error inesperado'}
        </Text>
        <TouchableOpacity
          style={s.crashButton}
          onPress={() => this.setState({ hasError: false, error: null })}
        >
          <Text style={s.crashButtonText}>Reintentar</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

function AppContent() {
  const [registrado, setRegistrado] = useState(null); // null=cargando, false=nuevo, true=vinculado
  const [errorInicio, setErrorInicio] = useState('');

  const verificarRegistro = () => {
    getSecureStore().getItemAsync('deviceToken')
      .then(token => {
        setRegistrado(!!token);
      })
      .catch(() => {
        setErrorInicio('No se pudo leer el almacenamiento seguro del dispositivo.');
        setRegistrado(false);
      });
  };

  useEffect(() => {
    verificarRegistro();
  }, []);

  if (registrado === null) return null; // splash nativo mientras carga

  return (
    <>
      <StatusBar style="dark" />
      {errorInicio ? (
        <View style={s.errorBox}>
          <Text style={s.errorText}>{errorInicio}</Text>
        </View>
      ) : null}
      {registrado
        ? <AsistenciaScreen onDesvincular={() => setRegistrado(false)} />
        : <RegistroScreen onRegistrado={() => setRegistrado(true)} />
      }
    </>
  );
}

export default function App() {
  return (
    <AppErrorBoundary>
      <AppContent />
    </AppErrorBoundary>
  );
}

const s = StyleSheet.create({
  errorBox: {
    backgroundColor: '#fef2f2',
    borderBottomWidth: 1,
    borderBottomColor: '#fecaca',
    padding: 10,
  },
  errorText: {
    color: '#991b1b',
    fontSize: 12,
    textAlign: 'center',
  },
  crashContainer: {
    flex: 1,
    backgroundColor: '#f8fafc',
    justifyContent: 'center',
    padding: 28,
  },
  crashTitle: {
    color: '#1e293b',
    fontSize: 24,
    fontWeight: '800',
    marginBottom: 10,
  },
  crashText: {
    color: '#475569',
    fontSize: 15,
    lineHeight: 22,
    marginBottom: 14,
  },
  crashDetail: {
    color: '#991b1b',
    backgroundColor: '#fee2e2',
    borderRadius: 10,
    padding: 12,
    fontSize: 12,
    marginBottom: 18,
  },
  crashButton: {
    backgroundColor: '#2563eb',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  crashButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
});

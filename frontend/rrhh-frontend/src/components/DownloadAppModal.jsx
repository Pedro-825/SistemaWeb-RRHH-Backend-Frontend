import { useState } from "react";
import { enviarLinkApp, confirmarAppInstalada } from "../services/api";

const PASO = { INICIO: "INICIO", CONFIRMACION: "CONFIRMACION" };

export default function DownloadAppModal({ idEmpleado, onClose, onInstalada }) {
  const [paso, setPaso] = useState(PASO.INICIO);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState("");

  const recordarNoMostrar = () => {
    if (idEmpleado) localStorage.setItem(`downloadAppDismissed:${idEmpleado}`, "true");
  };

  const handleEnviar = async () => {
    setEnviando(true);
    setError("");
    try {
      await enviarLinkApp(idEmpleado);
      setPaso(PASO.CONFIRMACION);
    } catch {
      setError("No se pudo enviar el correo. Verifica que tengas un correo registrado e intenta de nuevo.");
    } finally {
      setEnviando(false);
    }
  };

  const handleListo = async () => {
    try {
      await confirmarAppInstalada(idEmpleado);
    } catch {
      recordarNoMostrar();
    }
    onInstalada();
  };

  const handleMasTarde = () => {
    onClose();
  };

  const handleNoRecordar = () => {
    recordarNoMostrar();
    onClose();
  };

  return (
    <div style={{
      position: "fixed", inset: 0, background: "rgba(0,0,0,0.55)",
      display: "flex", alignItems: "center", justifyContent: "center",
      zIndex: 9000, fontFamily: "'Poppins', system-ui, sans-serif",
    }}>
      <div style={{
        background: "#fff", borderRadius: 18, padding: "34px 32px",
        maxWidth: 430, width: "90%", textAlign: "center",
        boxShadow: "0 20px 60px rgba(0,0,0,0.25)",
      }}>
        {paso === PASO.INICIO && (
          <>
            <div style={{ fontSize: 24, fontWeight: 800, color: "#2563eb", marginBottom: 10 }}>APP</div>
            <h3 style={{ margin: "0 0 10px", color: "#1e293b", fontSize: 19 }}>
              Registra tu asistencia desde tu celular
            </h3>
            <p style={{ color: "#475569", fontSize: 13, margin: "0 0 6px", lineHeight: 1.6 }}>
              Descarga la app del Hospital San Gabriel para registrar entrada y salida con huella dactilar.
            </p>
            <p style={{ color: "#94a3b8", fontSize: 12, margin: "0 0 20px" }}>
              Podemos enviarte el enlace de descarga a tu correo registrado.
            </p>

            {error && (
              <div style={{
                background: "#fef2f2", border: "1px solid #fca5a5", borderRadius: 8,
                padding: "10px 14px", marginBottom: 16, fontSize: 12,
                color: "#dc2626", textAlign: "left",
              }}>
                {error}
              </div>
            )}

            <div style={{ display: "flex", gap: 12, justifyContent: "center", flexWrap: "wrap" }}>
              <button
                onClick={handleMasTarde}
                style={{
                  flex: "1 1 120px", padding: "11px 0", borderRadius: 10,
                  border: "1px solid #cbd5e1", background: "#f8fafc",
                  color: "#475569", cursor: "pointer", fontSize: 14,
                }}
              >
                Mas tarde
              </button>
              <button
                onClick={handleEnviar}
                disabled={enviando}
                style={{
                  flex: "1 1 140px", padding: "11px 0", borderRadius: 10,
                  border: "none", background: "#2563eb",
                  color: "#fff", cursor: "pointer", fontSize: 14,
                  fontWeight: 600, opacity: enviando ? 0.7 : 1,
                }}
              >
                {enviando ? "Enviando..." : "Enviar al correo"}
              </button>
            </div>

            <button
              type="button"
              onClick={handleNoRecordar}
              style={{
                marginTop: 14, border: "none", background: "transparent",
                color: "#64748b", cursor: "pointer", fontSize: 12,
                textDecoration: "underline",
              }}
            >
              No volver a recordarmelo
            </button>
          </>
        )}

        {paso === PASO.CONFIRMACION && (
          <>
            <div style={{ fontSize: 24, fontWeight: 800, color: "#16a34a", marginBottom: 12 }}>OK</div>
            <h3 style={{ margin: "0 0 8px", color: "#1e293b", fontSize: 18 }}>
              Enlace enviado
            </h3>
            <p style={{ color: "#475569", fontSize: 13, margin: "0 0 6px", lineHeight: 1.6 }}>
              Revisa tu correo, descarga e instala la app. Cuando termines, marca la instalacion como lista.
            </p>

            <div style={{ display: "flex", gap: 12, justifyContent: "center", marginTop: 24, flexWrap: "wrap" }}>
              <button
                onClick={handleMasTarde}
                style={{
                  flex: "1 1 120px", padding: "11px 0", borderRadius: 10,
                  border: "1px solid #cbd5e1", background: "#f8fafc",
                  color: "#475569", cursor: "pointer", fontSize: 14, fontWeight: 600,
                }}
              >
                Aun falta
              </button>
              <button
                onClick={handleListo}
                style={{
                  flex: "1 1 120px", padding: "11px 0", borderRadius: 10,
                  border: "none", background: "#16a34a",
                  color: "#fff", cursor: "pointer", fontSize: 14, fontWeight: 600,
                }}
              >
                Ya la instale
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

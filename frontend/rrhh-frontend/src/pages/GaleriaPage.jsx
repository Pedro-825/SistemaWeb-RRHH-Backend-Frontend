import { useState, useEffect, useRef, useCallback } from "react";
import { getUser } from "../services/api";
import { listarGaleria, subirFotoGaleria, eliminarFotoGaleria } from "../services/api";

const BASE = import.meta.env.VITE_API_URL || '';
const resolveUrl = (url) => url?.startsWith('/') ? `${BASE}${url}` : url;
import Sidebar from "../components/Sidebar";

export default function GaleriaPage() {
  const user = getUser();
  const rol  = user?.rol ?? "EMPLEADO";

  const [fotos,     setFotos]     = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error,     setError]     = useState("");
  const [mensaje,   setMensaje]   = useState("");
  const inputRef = useRef(null);

  const cargar = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await listarGaleria(user?.idEmpleado);
      setFotos(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudo cargar la galería.");
    } finally {
      setLoading(false);
    }
  }, [user?.idEmpleado]);

  useEffect(() => { cargar(); }, [cargar]);

  async function handleSubir(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setError("");
    setMensaje("");
    try {
      await subirFotoGaleria(user?.idEmpleado, file);
      setMensaje("Foto subida correctamente.");
      await cargar();
    } catch {
      setError("Error al subir la foto. Intente de nuevo.");
    } finally {
      setUploading(false);
      e.target.value = "";
    }
  }

  async function handleEliminar(id) {
    setError("");
    setMensaje("");
    try {
      await eliminarFotoGaleria(id);
      setFotos(prev => prev.filter(f => f.id !== id));
      setMensaje("Foto eliminada.");
    } catch {
      setError("Error al eliminar la foto.");
    }
  }

  return (
    <div className="app-layout">
      <Sidebar rol={rol} />
      <main className="main-content">
        <div className="main-header">
          <div>
            <h1 style={{ fontSize: 20, fontWeight: 700, margin: 0 }}>📷 Mi Galería de Evidencias</h1>
            <p style={{ margin: "4px 0 0", fontSize: 13, color: "var(--text-3)" }}>
              Sube fotos para adjuntar como evidencia en tus justificaciones.
            </p>
          </div>
        </div>

        <div className="section-card">
          {error   && <div className="alert alert-warning" style={{ marginBottom: 16 }}>⚠️ {error}</div>}
          {mensaje && <div className="alert alert-success" style={{ marginBottom: 16 }}>✅ {mensaje}</div>}

          {/* ── Sección 1: Subir imagen ── */}
          <div>
            <h3 style={{ fontSize: 14, fontWeight: 600, color: "var(--text-2)", marginBottom: 12 }}>
              Subir nueva imagen
            </h3>
            <input
              ref={inputRef}
              type="file"
              accept="image/*"
              style={{ display: "none" }}
              onChange={handleSubir}
            />
            <div
              onClick={() => !uploading && inputRef.current?.click()}
              style={{
                border: "2px dashed var(--border)",
                borderRadius: 12,
                padding: "32px 24px",
                textAlign: "center",
                cursor: uploading ? "default" : "pointer",
                background: uploading ? "var(--bg-2)" : "#f8fafc",
                transition: "border-color 0.2s, background 0.2s",
              }}
              onMouseEnter={e => { if (!uploading) e.currentTarget.style.borderColor = "var(--primary)"; }}
              onMouseLeave={e => { e.currentTarget.style.borderColor = "var(--border)"; }}
            >
              {uploading ? (
                <>
                  <div style={{ fontSize: 36, marginBottom: 8 }}>⏳</div>
                  <p style={{ margin: 0, fontWeight: 600, color: "var(--primary)" }}>Subiendo imagen...</p>
                </>
              ) : (
                <>
                  <div style={{ fontSize: 36, marginBottom: 8 }}>🖼️</div>
                  <p style={{ margin: 0, fontWeight: 600, color: "var(--text-1)" }}>Haz clic para seleccionar una imagen</p>
                  <p style={{ margin: "4px 0 0", fontSize: 12, color: "var(--text-3)" }}>
                    PNG, JPG, JPEG — Máx. 10 MB
                  </p>
                </>
              )}
            </div>
          </div>

          {/* ── Separador ── */}
          <hr style={{ margin: "24px 0", border: "none", borderTop: "1px solid var(--border)" }} />

          {/* ── Sección 2: Fotos guardadas ── */}
          <div>
            <h3 style={{ fontSize: 14, fontWeight: 600, color: "var(--text-2)", marginBottom: 12 }}>
              Fotos guardadas
              {fotos.length > 0 && (
                <span style={{ marginLeft: 8, fontWeight: 400, color: "var(--text-3)" }}>
                  ({fotos.length} {fotos.length === 1 ? "foto" : "fotos"})
                </span>
              )}
            </h3>

            {loading ? (
              <div className="loading-text">Cargando galería...</div>
            ) : fotos.length === 0 ? (
              <div className="empty-state" style={{ padding: "24px 0" }}>
                <div className="empty-state-icon">📂</div>
                <p>No tienes fotos guardadas aún</p>
                <p style={{ fontSize: 12, color: "var(--text-3)" }}>
                  Sube una imagen desde la sección de arriba
                </p>
              </div>
            ) : (
              <div className="galeria-page-grid">
                {fotos.map(foto => (
                  <div key={foto.id} className="galeria-page-item">
                    <img
                      src={resolveUrl(foto.url)}
                      alt={foto.nombreArchivo || "Evidencia"}
                      className="galeria-page-img"
                    />
                    <div className="galeria-page-item-fecha">
                      📅 {foto.fechaSubida
                        ? new Date(foto.fechaSubida).toLocaleDateString("es-PE", { day: "2-digit", month: "short", year: "numeric" })
                        : "—"}
                    </div>
                    <div className="galeria-page-item-actions">
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => handleEliminar(foto.id)}
                        title="Eliminar foto"
                      >
                        🗑 Eliminar
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

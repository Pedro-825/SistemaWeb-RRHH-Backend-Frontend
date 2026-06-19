import { Component } from "react";

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    console.error("[ErrorBoundary]", error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          minHeight: "100vh",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: "16px",
          padding: "32px",
          background: "#fef2f2",
          fontFamily: "'Poppins', system-ui, sans-serif",
          textAlign: "center",
        }}>
          <div style={{ fontSize: "64px" }}>⚠️</div>
          <h2 style={{ margin: 0, color: "#991b1b", fontSize: "20px" }}>
            Algo salió mal
          </h2>
          <p style={{ color: "#b91c1c", fontSize: "14px", maxWidth: "480px" }}>
            Ocurrió un error inesperado en la aplicación. Recarga la página para continuar.
          </p>
          <button
            onClick={() => window.location.reload()}
            style={{
              padding: "10px 24px",
              background: "#dc2626",
              color: "#fff",
              border: "none",
              borderRadius: "8px",
              fontWeight: 600,
              cursor: "pointer",
              fontSize: "14px",
            }}
          >
            🔄 Recargar página
          </button>
          {this.props.showError && (
            <pre style={{
              background: "#f1f5f9",
              padding: "12px 16px",
              borderRadius: "8px",
              fontSize: "11px",
              color: "#64748b",
              maxWidth: "600px",
              overflow: "auto",
              textAlign: "left",
            }}>
              {this.state.error?.message}
            </pre>
          )}
        </div>
      );
    }

    return this.props.children;
  }
}

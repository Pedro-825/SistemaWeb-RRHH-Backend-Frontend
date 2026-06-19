const MESES = ["Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"];

const css = (accentColor) => `
body{font-family:Arial,sans-serif;font-size:12px;padding:28px;max-width:680px;margin:0 auto;color:#1e293b}
.hdr{display:flex;align-items:center;gap:16px;margin-bottom:20px;padding-bottom:14px;border-bottom:2px solid ${accentColor}}
.hdr img{height:60px}
.hdr h1{margin:0 0 2px;font-size:18px;color:#1e293b}
.hdr p{margin:0;font-size:11px;color:#64748b}
.titulo{font-size:14px;font-weight:700;color:${accentColor};margin:0 0 16px;text-align:center}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:20px;background:#f8fafc;padding:14px;border-radius:8px}
.gi{display:flex;flex-direction:column;gap:2px}
.gl{font-size:10px;color:#64748b;text-transform:uppercase;letter-spacing:0.5px}
.gv{font-weight:600;font-size:13px}
.sec{margin-bottom:16px}
.st{font-size:12px;font-weight:700;padding:6px 10px;border-radius:4px;margin-bottom:8px}
.gt{background:#dcfce7;color:#15803d}
.rt{background:#fee2e2;color:#b91c1c}
.row{display:flex;justify-content:space-between;padding:5px 10px;font-size:12px}
.row:nth-child(even){background:#f8fafc}
.sub{display:flex;justify-content:space-between;padding:6px 10px;font-weight:700;border-top:1px dashed #cbd5e1;margin-top:4px}
.total{background:${accentColor};color:#fff;border-radius:8px;padding:16px 20px;display:flex;justify-content:space-between;align-items:center;margin-top:20px}
.tl{font-size:14px;font-weight:600}
.tv{font-size:22px;font-weight:800}
.footer{margin-top:24px;font-size:10px;color:#94a3b8;text-align:center;border-top:1px solid #e2e8f0;padding-top:12px}
.pbtn{display:block;margin:16px auto 0;padding:9px 22px;background:${accentColor};color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px}
@media print{.pbtn{display:none}}
`;

export function generarComprobanteHTML(n, logoUrl, accentColor = "#2563eb", _title = "Comprobante de Nómina", entity = "Sistema de Recursos Humanos") {
  const base = Number(n.sueldoBase ?? n.sueldo ?? 0);
  const getBonif = (o) =>
    (Number(o.bonifFamiliar)      || 0) +
    (Number(o.bonifTurnoNocturno) || 0) +
    (Number(o.bonifGuardia)       || 0) +
    (Number(o.bonifRiesgo)        || 0) +
    (Number(o.bonifCargo)         || 0);
  const getDesc = (o) =>
    (Number(o.descuentoTardanzas) || 0) +
    (Number(o.descuentoLey)       || 0);
  const bonif = getBonif(n);
  const desc  = getDesc(n);
  const neto  = Number(n.sueldoNeto ?? 0);
  const periodo = n.periodo ?? `${n.anio}-${n.mes}`;
  const partes = periodo?.split("-") || [];
  const mesNum = parseInt(partes[1]);
  const mesLabel = !isNaN(mesNum) ? MESES[mesNum - 1] : "—";
  const anio = partes[0] ?? "—";
  const bonifs = [
    { label: "Bonif. Familiar",        val: n.bonifFamiliar },
    { label: "Bonif. Turno Nocturno",  val: n.bonifTurnoNocturno },
    { label: "Bonif. Guardia",         val: n.bonifGuardia },
    { label: "Bonif. Riesgo",          val: n.bonifRiesgo },
    { label: "Bonif. Cargo",           val: n.bonifCargo },
  ].filter(r => Number(r.val));
  const descs = [
    { label: "Descuento Tardanzas", val: n.descuentoTardanzas },
    { label: "Descuento de Ley",    val: n.descuentoLey },
  ].filter(r => Number(r.val));
  const fmt      = (v) => `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}`;

  return `<!DOCTYPE html><html lang="es"><head>
<meta charset="utf-8"><title>Comprobante Nómina #${n.idNomina}</title>
<style>${css(accentColor)}</style></head><body>
<div class="hdr">
  <img src="${logoUrl}" alt="Logo" onerror="this.style.display='none'" />
  <div><h1>Hospital San Gabriel</h1><p>${entity}</p></div>
</div>
<div class="titulo">BOLETA DE PAGO — ${mesLabel.toUpperCase()} ${anio}</div>
<div class="grid">
  <div class="gi"><span class="gl">Empleado</span><span class="gv">${n.empleadoNombre ?? n.nombreEmpleado ?? `Empleado #${n.idEmpleado}`}</span></div>
  <div class="gi"><span class="gl">N° Nómina</span><span class="gv">#${n.idNomina}</span></div>
  <div class="gi"><span class="gl">Período</span><span class="gv">${mesLabel} ${anio}</span></div>
  <div class="gi"><span class="gl">Sueldo Base</span><span class="gv">${fmt(base)}</span></div>
</div>
<div class="sec">
  <div class="st gt">+ BONIFICACIONES</div>
  ${bonifs.map(r => `<div class="row"><span>${r.label}</span><span style="color:#15803d">+${fmt(r.val)}</span></div>`).join('')}
  <div class="sub"><span>Total Bonificaciones</span><span style="color:#15803d">+${fmt(bonif)}</span></div>
</div>
<div class="sec">
  <div class="st rt">— DESCUENTOS</div>
  ${descs.map(r => `<div class="row"><span>${r.label}</span><span style="color:#b91c1c">-${fmt(r.val)}</span></div>`).join('')}
  <div class="sub"><span>Total Descuentos</span><span style="color:#b91c1c">-${fmt(desc)}</span></div>
</div>
<div class="total"><span class="tl">SUELDO NETO A PAGAR</span><span class="tv">${fmt(neto)}</span></div>
<div class="footer">Documento generado automáticamente por el sistema RRHH — Hospital San Gabriel<br>${new Date().toLocaleString("es-PE")}</div>
<button class="pbtn" onclick="window.print()">🖨 Imprimir / Guardar como PDF</button>
</body></html>`;
}

export function abrirComprobante(n, logoUrl, accentColor = "#2563eb", _title, entity) {
  const html = generarComprobanteHTML(n, logoUrl, accentColor, _title, entity);
  const blob = new Blob([html], { type: "text/html;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  window.open(url, "_blank");
  setTimeout(() => URL.revokeObjectURL(url), 10000);
}

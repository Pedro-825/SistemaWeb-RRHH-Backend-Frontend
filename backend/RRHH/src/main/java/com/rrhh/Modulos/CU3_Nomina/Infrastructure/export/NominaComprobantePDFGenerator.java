package com.rrhh.Modulos.CU3_Nomina.Infrastructure.export;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.DetalleNominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class NominaComprobantePDFGenerator {

    // ── Fuentes ──────────────────────────────────────────────────────
    private static final Font F_TITLE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  16, new Color(255, 255, 255));
    private static final Font F_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA,        9, new Color(200, 220, 255));
    private static final Font F_SEC      = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, new Color(255, 255, 255));
    private static final Font F_LABEL    = FontFactory.getFont(FontFactory.HELVETICA,        9, new Color(60, 60, 60));
    private static final Font F_VALUE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, new Color(20, 20, 20));
    private static final Font F_NEG      = FontFactory.getFont(FontFactory.HELVETICA,        9, new Color(180, 30, 30));
    private static final Font F_NETO_LBL = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  13, new Color(21, 128, 61));
    private static final Font F_NETO_VAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  16, new Color(21, 128, 61));
    private static final Font F_TH       = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, new Color(255, 255, 255));
    private static final Font F_TD       = FontFactory.getFont(FontFactory.HELVETICA,         8, new Color(30, 30, 30));
    private static final Font F_TOTAL    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, new Color(20, 20, 20));
    // ── Colores ───────────────────────────────────────────────────────
    private static final Color C_HEADER  = new Color(15,  52,  96);   // azul oscuro
    private static final Color C_SEC_HDR = new Color(30,  80, 160);   // azul sección
    private static final Color C_TH_DET  = new Color(55, 110, 180);   // azul tabla detalle
    private static final Color C_ROW_ALT = new Color(240, 245, 255);  // fila alterna
    private static final Color C_NETO_BG = new Color(220, 252, 231);  // verde claro
    private static final Color C_BORDER  = new Color(180, 200, 230);
    private static final Color C_TOTAL   = new Color(225, 235, 250);  // fila total

    public byte[] generate(NominaResponseDTO n) {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc, n);
            addSpace(doc, 10);
            addEmployeeInfo(doc, n);
            addSpace(doc, 8);
            addSalarySection(doc, n);
            addSpace(doc, 8);
            addNetoBox(doc, n);
            addSpace(doc, 12);
            addDetalleDiario(doc, n);
            addSpace(doc, 12);
            addFooter(doc);

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar comprobante PDF", e);
        }
        return baos.toByteArray();
    }

    // ── 1. Encabezado ─────────────────────────────────────────────────
    private void addHeader(Document doc, NominaResponseDTO n) throws Exception {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);

        PdfPCell title = new PdfPCell(new Phrase("COMPROBANTE DE NÓMINA", F_TITLE));
        title.setBackgroundColor(C_HEADER);
        title.setHorizontalAlignment(Element.ALIGN_CENTER);
        title.setPadding(10);
        title.setBorder(Rectangle.NO_BORDER);
        t.addCell(title);

        String sub = "Período: " + (n.getPeriodo() != null ? n.getPeriodo() : "—")
                + "    |    Fecha de emisión: " + LocalDate.now();
        PdfPCell subCell = new PdfPCell(new Phrase(sub, F_SUBTITLE));
        subCell.setBackgroundColor(C_HEADER);
        subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        subCell.setPadding(4);
        subCell.setBorder(Rectangle.NO_BORDER);
        t.addCell(subCell);

        doc.add(t);
    }

    // ── 2. Datos del empleado ─────────────────────────────────────────
    private void addEmployeeInfo(Document doc, NominaResponseDTO n) throws Exception {
        PdfPTable t = new PdfPTable(new float[]{1, 1});
        t.setWidthPercentage(100);
        t.setSpacingBefore(0);

        // Header fila completa
        PdfPCell hdr = new PdfPCell(new Phrase("DATOS DEL EMPLEADO", F_SEC));
        hdr.setColspan(2);
        hdr.setBackgroundColor(C_SEC_HDR);
        hdr.setPadding(5);
        hdr.setBorderColor(C_BORDER);
        t.addCell(hdr);

        String nombre = n.getNombreEmpleado() != null ? n.getNombreEmpleado() : "—";
        addInfoRow(t, "Empleado", nombre, "Período", n.getPeriodo() != null ? n.getPeriodo() : "—");
        addInfoRow(t, "Estado de pago", n.getEstadoPago() != null ? n.getEstadoPago() : "—",
                "Fecha inicio", n.getFechaInicio() != null ? n.getFechaInicio().toString() : "—");
        addInfoRow(t, "Fecha fin", n.getFechaFin() != null ? n.getFechaFin().toString() : "—",
                "Fecha emisión", LocalDate.now().toString());

        doc.add(t);
    }

    private void addInfoRow(PdfPTable t, String lbl1, String val1, String lbl2, String val2) {
        PdfPCell c1 = new PdfPCell();
        c1.addElement(new Phrase(lbl1 + ": ", F_LABEL));
        c1.addElement(new Phrase(val1, F_VALUE));
        c1.setPadding(5); c1.setBorderColor(C_BORDER);
        t.addCell(c1);

        PdfPCell c2 = new PdfPCell();
        c2.addElement(new Phrase(lbl2 + ": ", F_LABEL));
        c2.addElement(new Phrase(val2, F_VALUE));
        c2.setPadding(5); c2.setBorderColor(C_BORDER);
        t.addCell(c2);
    }

    // ── 3. Sección Remuneraciones / Descuentos ─────────────────────────
    private void addSalarySection(Document doc, NominaResponseDTO n) throws Exception {
        PdfPTable t = new PdfPTable(new float[]{1, 1});
        t.setWidthPercentage(100);

        // Headers de columna
        PdfPCell hLeft = new PdfPCell(new Phrase("REMUNERACIONES", F_SEC));
        hLeft.setBackgroundColor(C_SEC_HDR); hLeft.setPadding(5); hLeft.setBorderColor(C_BORDER);
        t.addCell(hLeft);
        PdfPCell hRight = new PdfPCell(new Phrase("DESCUENTOS", F_SEC));
        hRight.setBackgroundColor(new Color(180, 30, 30)); hRight.setPadding(5); hRight.setBorderColor(C_BORDER);
        t.addCell(hRight);

        // Filas de remuneraciones vs descuentos
        addSalaryRow(t, "Sueldo Base", n.getSueldoBase(), false,
                "AFP / Desc. de Ley", n.getDescuentoLey(), true, false);
        addSalaryRow(t, "Bonif. Familiar", n.getBonifFamiliar(), false,
                "Desc. Tardanzas", n.getDescuentoTardanzas(), true, true);
        addSalaryRow(t, "Bonif. Turno Nocturno", n.getBonifTurnoNocturno(), false,
                "", null, false, false);
        addSalaryRow(t, "Bonif. Guardia", n.getBonifGuardia(), false,
                "", null, false, true);
        addSalaryRow(t, "Bonif. Riesgo", n.getBonifRiesgo(), false,
                "", null, false, false);
        addSalaryRow(t, "Bonif. Cargo", n.getBonifCargo(), false,
                "", null, false, true);

        // Fila total
        addTotalRow(t, "TOTAL BRUTO", n.getSueldoBruto(),
                "TOTAL DESCUENTOS", totalDesc(n));

        doc.add(t);
    }

    private void addSalaryRow(PdfPTable t,
                              String lbl1, BigDecimal val1, boolean neg1,
                              String lbl2, BigDecimal val2, boolean neg2,
                              boolean alt) {
        Color bg = alt ? C_ROW_ALT : Color.WHITE;

        PdfPCell left = buildSalaryCell(lbl1, val1, neg1, bg);
        PdfPCell right = buildSalaryCell(lbl2, val2, neg2, bg);
        t.addCell(left);
        t.addCell(right);
    }

    private PdfPCell buildSalaryCell(String label, BigDecimal value, boolean negative, Color bg) {
        PdfPTable inner = new PdfPTable(new float[]{3, 2});
        inner.setWidthPercentage(100);

        PdfPCell lCell = new PdfPCell(new Phrase(label, F_LABEL));
        lCell.setBorder(Rectangle.NO_BORDER); lCell.setPaddingLeft(4); lCell.setPaddingTop(3); lCell.setPaddingBottom(3);
        inner.addCell(lCell);

        String formatted = value != null ? fmt(value) : "";
        Font font = negative ? F_NEG : F_VALUE;
        PdfPCell vCell = new PdfPCell(new Phrase(formatted, font));
        vCell.setBorder(Rectangle.NO_BORDER); vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vCell.setPaddingRight(4); vCell.setPaddingTop(3); vCell.setPaddingBottom(3);
        inner.addCell(vCell);

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setBackgroundColor(bg);
        wrapper.setBorderColor(C_BORDER);
        wrapper.setPadding(0);
        return wrapper;
    }

    private void addTotalRow(PdfPTable t, String lbl1, BigDecimal val1, String lbl2, BigDecimal val2) {
        PdfPCell left = buildTotalCell(lbl1, val1);
        PdfPCell right = buildTotalCell(lbl2, val2);
        t.addCell(left);
        t.addCell(right);
    }

    private PdfPCell buildTotalCell(String label, BigDecimal value) {
        PdfPTable inner = new PdfPTable(new float[]{3, 2});
        inner.setWidthPercentage(100);

        PdfPCell lc = new PdfPCell(new Phrase(label, F_TOTAL));
        lc.setBorder(Rectangle.NO_BORDER); lc.setPaddingLeft(4); lc.setPaddingTop(5); lc.setPaddingBottom(5);
        inner.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value != null ? fmt(value) : "—", F_TOTAL));
        vc.setBorder(Rectangle.NO_BORDER); vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setPaddingRight(4); vc.setPaddingTop(5); vc.setPaddingBottom(5);
        inner.addCell(vc);

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setBackgroundColor(C_TOTAL);
        wrapper.setBorderColor(C_BORDER);
        wrapper.setPadding(0);
        return wrapper;
    }

    // ── 4. Caja Neto ──────────────────────────────────────────────────
    private void addNetoBox(Document doc, NominaResponseDTO n) throws Exception {
        PdfPTable t = new PdfPTable(new float[]{1, 1});
        t.setWidthPercentage(60);
        t.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell lbl = new PdfPCell(new Phrase("NETO A PAGAR", F_NETO_LBL));
        lbl.setBackgroundColor(C_NETO_BG); lbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lbl.setPadding(10); lbl.setBorderColor(new Color(134, 239, 172));
        t.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(n.getSueldoNeto() != null ? fmt(n.getSueldoNeto()) : "—", F_NETO_VAL));
        val.setBackgroundColor(C_NETO_BG); val.setHorizontalAlignment(Element.ALIGN_CENTER);
        val.setVerticalAlignment(Element.ALIGN_MIDDLE);
        val.setPadding(10); val.setBorderColor(new Color(134, 239, 172));
        t.addCell(val);

        doc.add(t);
    }

    // ── 5. Detalle diario ─────────────────────────────────────────────
    private void addDetalleDiario(Document doc, NominaResponseDTO n) throws Exception {
        if (n.getDetalles() == null || n.getDetalles().isEmpty()) return;

        Paragraph title = new Paragraph("DETALLE DIARIO", F_TOTAL);
        title.setSpacingBefore(4); title.setSpacingAfter(4);
        doc.add(title);

        String[] headers = {"Fecha", "Hs. Trabajadas", "Min. Tardanza", "Desc. Tardanza", "Min. Extra", "Monto Extra"};
        float[] widths = {2f, 2f, 1.8f, 2f, 1.8f, 2f};

        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, F_TH));
            cell.setBackgroundColor(C_TH_DET);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4); cell.setBorderColor(C_BORDER);
            t.addCell(cell);
        }

        boolean alt = false;
        for (DetalleNominaResponseDTO d : n.getDetalles()) {
            Color bg = alt ? C_ROW_ALT : Color.WHITE;
            addDetalleRow(t, bg,
                    d.getFecha() != null ? d.getFecha().toString() : "—",
                    d.getHorasTrabajadas() != null ? d.getHorasTrabajadas().toPlainString() : "0",
                    String.valueOf(d.getMinutosTardanza() != null ? d.getMinutosTardanza() : 0),
                    d.getDescuentoTardanza() != null ? fmt(d.getDescuentoTardanza()) : "S/ 0.00",
                    String.valueOf(d.getMinutosExtra() != null ? d.getMinutosExtra() : 0),
                    d.getMontoHoraExtra() != null ? fmt(d.getMontoHoraExtra()) : "S/ 0.00");
            alt = !alt;
        }

        // Fila resumen total
        BigDecimal totalHs   = n.getTotalHorasTrabajadas() != null ? n.getTotalHorasTrabajadas() : BigDecimal.ZERO;
        BigDecimal totalExtra = n.getTotalHorasExtra() != null ? n.getTotalHorasExtra() : BigDecimal.ZERO;
        int totalMins = n.getTotalMinutosTardanza() != null ? n.getTotalMinutosTardanza() : 0;
        BigDecimal descTard  = n.getDescuentoTardanzas() != null ? n.getDescuentoTardanzas() : BigDecimal.ZERO;

        String[] totals = {"TOTALES", totalHs.toPlainString(), String.valueOf(totalMins),
                fmt(descTard), totalExtra.toPlainString(), "—"};
        for (String v : totals) {
            PdfPCell cell = new PdfPCell(new Phrase(v, F_TOTAL));
            cell.setBackgroundColor(C_TOTAL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4); cell.setBorderColor(C_BORDER);
            t.addCell(cell);
        }

        doc.add(t);
    }

    private void addDetalleRow(PdfPTable t, Color bg, String... vals) {
        for (String v : vals) {
            PdfPCell cell = new PdfPCell(new Phrase(v, F_TD));
            cell.setBackgroundColor(bg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3); cell.setBorderColor(C_BORDER);
            t.addCell(cell);
        }
    }

    // ── 6. Pie de página ──────────────────────────────────────────────
    private void addFooter(Document doc) throws Exception {
        Font small = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(150, 150, 150));
        Paragraph p = new Paragraph("Documento generado automáticamente por el Sistema RRHH — Hospital San Gabriel. No requiere firma.", small);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private void addSpace(Document doc, float pts) throws Exception {
        Paragraph sp = new Paragraph(" ");
        sp.setSpacingAfter(pts);
        doc.add(sp);
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private String fmt(BigDecimal v) {
        if (v == null) return "S/ 0.00";
        return "S/ " + String.format("%,.2f", v.doubleValue());
    }

    private BigDecimal totalDesc(NominaResponseDTO n) {
        BigDecimal ley  = n.getDescuentoLey()       != null ? n.getDescuentoLey()       : BigDecimal.ZERO;
        BigDecimal tard = n.getDescuentoTardanzas()  != null ? n.getDescuentoTardanzas() : BigDecimal.ZERO;
        return ley.add(tard);
    }
}

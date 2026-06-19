package com.rrhh.Shared.export.pdf.implementations;

import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class GenericPDFExporter implements PDFExporter {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_CELL = FontFactory.getFont(FontFactory.HELVETICA, 9);

    @Override
    public byte[] export(String titulo, List<Map<String, Object>> datos, String[] headers, String[] keys) {
        if (datos == null || datos.isEmpty()) {
            return new byte[0];
        }

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Paragraph titleParagraph = new Paragraph(titulo, FONT_TITLE);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(15);
            document.add(titleParagraph);

            Paragraph dateParagraph = new Paragraph("Fecha de generacion: " + LocalDate.now(), FONT_CELL);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            dateParagraph.setSpacingAfter(10);
            document.add(dateParagraph);

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FONT_HEADER));
                cell.setGrayFill(0.85f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(4);
                table.addCell(cell);
            }

            for (Map<String, Object> row : datos) {
                for (String key : keys) {
                    String valor = formatValue(row.get(key));
                    PdfPCell cell = new PdfPCell(new Phrase(valor, FONT_CELL));
                    cell.setPadding(3);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }

        return baos.toByteArray();
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof LocalDateTime) return ((LocalDateTime) value).toLocalDate().toString();
        if (value instanceof LocalDate) return value.toString();
        if (value instanceof LocalTime) return value.toString();
        return value.toString();
    }
}

package com.rrhh.Shared.export.excel.implementations;

import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class GenericExcelExporter implements ExcelExporter {

    @Override
    public byte[] export(String titulo, List<Map<String, Object>> datos, String[] headers, String[] keys) {
        if (datos == null || datos.isEmpty()) {
            return new byte[0];
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(titulo);

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            int rowNum = 0;

            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            rowNum++;

            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Fecha de generacion: " + LocalDate.now());

            rowNum++;

            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Map<String, Object> dato : datos) {
                Row dataRow = sheet.createRow(rowNum++);
                for (int i = 0; i < keys.length; i++) {
                    String valor = formatValue(dato.get(keys[i]));
                    dataRow.createCell(i).setCellValue(valor);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel", e);
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof LocalDateTime) return ((LocalDateTime) value).toLocalDate().toString();
        if (value instanceof LocalDate) return value.toString();
        if (value instanceof LocalTime) return value.toString();
        return value.toString();
    }
}

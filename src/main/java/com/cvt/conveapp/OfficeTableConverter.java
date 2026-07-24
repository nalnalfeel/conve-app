package com.cvt.conveapp;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class OfficeTableConverter {

    public static void wordToExcel(File wordFile, File excelOutputFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(wordFile);
             XWPFDocument document = new XWPFDocument(fis);
             Workbook workbook = new XSSFWorkbook()) {

            int sheetIndex = 1;
            if (document.getTables().isEmpty()) {
                // Jika tidak ada tabel di Word, buat sheet kosong dengan pesan
                Sheet sheet = workbook.createSheet("Pesan");
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("Dokumen Word ini tidak memiliki tabel.");
            } else {
                for (XWPFTable table : document.getTables()) {
                    Sheet sheet = workbook.createSheet("Tabel " + sheetIndex++);
                    int rowIndex = 0;
                    for (XWPFTableRow row : table.getRows()) {
                        Row excelRow = sheet.createRow(rowIndex++);
                        int colIndex = 0;
                        for (XWPFTableCell cell : row.getTableCells()) {
                            Cell excelCell = excelRow.createCell(colIndex++);
                            excelCell.setCellValue(cell.getText().trim());
                        }
                    }
                    if (rowIndex > 0 && sheet.getRow(0) != null) {
                        int colCount = sheet.getRow(0).getLastCellNum();
                        for (int i = 0; i < colCount; i++) {
                            sheet.autoSizeColumn(i);
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(excelOutputFile)) {
                workbook.write(fos);
                fos.flush(); // Memastikan buffer file tertulis ke disk
            }
        }
    }

    public static void excelToWOrd(File excelFile, File wordOutputFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis);
             XWPFDocument document = new XWPFDocument()) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("Sheet excel kosong!");
            }

            int rowCount = sheet.getPhysicalNumberOfRows();
            int maxColCount = 0;
            for (Row row : sheet) {
                if (row != null && row.getLastCellNum() > maxColCount) {
                    maxColCount = row.getLastCellNum();
                }
            }

            XWPFTable wordTable = document.createTable(rowCount, maxColCount);
            DataFormatter formatter = new DataFormatter();
            int wordRowIdx = 0;

            for (Row excelRow : sheet) {
                XWPFTableRow wordRow = wordTable.getRow(wordRowIdx++);
                if (wordRow == null) {
                    wordRow = wordTable.createRow();
                }

                for (int colIdx = 0; colIdx < maxColCount; colIdx++) {
                    Cell excelCell = (excelRow != null) ? excelRow.getCell(colIdx) : null;
                    String cellValue = (excelCell != null) ? formatter.formatCellValue(excelCell) : "";

                    XWPFTableCell wordCell = wordRow.getCell(colIdx);
                    if (wordCell == null) {
                        wordCell = wordRow.addNewTableCell();
                    }
                    wordCell.setText(cellValue);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(wordOutputFile)) {
                document.write(fos);
                fos.flush();
            }
        }
    }
}
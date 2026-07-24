package com.cvt.conveapp;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WordPdfConverter {

    /**
     * 1. Konversi Word (.docx) ke PDF (Dioperasikan via POI & PDFBox secara aman)
     */
    public static void wordToPdf(File inputDocx, File outputPdf) throws Exception {
        System.out.println("[INFO] Memproses konversi Word ke PDF: " + inputDocx.getName());

        try (FileInputStream fis = new FileInputStream(inputDocx);
             XWPFDocument docx = new XWPFDocument(fis);
             PDDocument pdfDoc = new PDDocument()) {

            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float fontSize = 11f;
            float leading = 14.5f;

            float margin = 40f;
            float startX = margin;
            float startY = 750f;
            float maxY = 50f;
            float maxWidth = PDRectangle.A4.getWidth() - (2 * margin);

            PDPage currentPage = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(currentPage);

            PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, currentPage);
            contentStream.setFont(font, fontSize);

            float currentY = startY;

            for (IBodyElement element : docx.getBodyElements()) {
                String extractedText = "";

                if (element instanceof XWPFParagraph) {
                    XWPFParagraph p = (XWPFParagraph) element;
                    extractedText = p.getText();

                    // Fallback jika p.getText() kosong tapi ada runs
                    if ((extractedText == null || extractedText.isEmpty()) && !p.getRuns().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (XWPFRun r : p.getRuns()) {
                            if (r.text() != null) sb.append(r.text());
                        }
                        extractedText = sb.toString();
                    }
                } else if (element instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) element;
                    StringBuilder sb = new StringBuilder();
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            sb.append(cell.getText().trim()).append(" | ");
                        }
                        sb.append("\n");
                    }
                    extractedText = sb.toString();
                }

                if (extractedText == null || extractedText.trim().isEmpty()) {
                    continue;
                }

                String[] rawLines = extractedText.split("\\r?\\n");
                for (String rawLine : rawLines) {
                    // Membersihkan karakter non-printable yang tidak didukung PDF Standard Font
                    String cleanLine = rawLine.replaceAll("[^\\x20-\\x7E]", " ").trim();
                    if (cleanLine.isEmpty()) continue;

                    List<String> wrappedLines = wrapText(cleanLine, font, fontSize, maxWidth);

                    for (String line : wrappedLines) {
                        if (currentY <= maxY) {
                            contentStream.close();
                            currentPage = new PDPage(PDRectangle.A4);
                            pdfDoc.addPage(currentPage);
                            contentStream = new PDPageContentStream(pdfDoc, currentPage);
                            // HAPUS contentStream.setFont() dari sini
                            currentY = startY;
                        }

                        contentStream.beginText();
                        // PINDAHKAN pemanggilan setFont ke sini, TEPAT SETELAH beginText()
                        contentStream.setFont(font, fontSize);
                        contentStream.newLineAtOffset(startX, currentY);
                        contentStream.showText(line);
                        contentStream.endText();

                        currentY -= leading;
                    }
                    /*for (String line : wrappedLines) {
                        if (currentY <= maxY) {
                            contentStream.close();

                            currentPage = new PDPage(PDRectangle.A4);
                            pdfDoc.addPage(currentPage);
                            contentStream = new PDPageContentStream(pdfDoc, currentPage);
                            contentStream.setFont(font, fontSize);
                            currentY = startY;
                        }

                        contentStream.beginText();
                        contentStream.newLineAtOffset(startX, currentY);
                        contentStream.showText(line);
                        contentStream.endText();

                        currentY -= leading;
                    }*/
                }
            }

            contentStream.close();
            pdfDoc.save(outputPdf);
            System.out.println("[SUCCESS] Konversi berhasil disimpan di: " + outputPdf.getAbsolutePath());
        }
    }

    private static List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws Exception {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String tempLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float width = font.getStringWidth(tempLine) / 1000 * fontSize;

            if (width > maxWidth) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(tempLine);
                    currentLine = new StringBuilder();
                }
            } else {
                currentLine.append(currentLine.length() == 0 ? "" : " ").append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * 2. Konversi PDF ke Word (.docx)
     */
    public static void pdfToWord(File inputPdf, File outputDocx) throws Exception {
        try (PDDocument pdfDocument = Loader.loadPDF(inputPdf);
             XWPFDocument docxDocument = new XWPFDocument()) {

            PDFTextStripper textStripper = new PDFTextStripper();
            // PERBAIKAN: Menginstruksikan PDFBox untuk mengurutkan teks berdasarkan koordinat posisinya
            textStripper.setSortByPosition(true);

            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
            int totalPages = pdfDocument.getNumberOfPages();

            for (int i = 1; i <= totalPages; i++) {
                textStripper.setStartPage(i);
                textStripper.setEndPage(i);
                String pageText = textStripper.getText(pdfDocument);

                if (pageText != null && !pageText.trim().isEmpty()) {
                    writeTextToWord(docxDocument, pageText);
                } else {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(i - 1, 300);
                    try {
                        String ocrResult = OcrService.doOcr(image);
                        writeTextToWord(docxDocument, "[Hasil OCR Halaman " + i + "]:\n" + ocrResult);
                    } catch (Exception e) {
                        writeTextToWord(docxDocument, "[Gagal OCR Halaman " + i + "]: " + e.getMessage());
                    }
                }

                if (i < totalPages) {
                    docxDocument.createParagraph().setPageBreak(true);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputDocx)) {
                docxDocument.write(fos);
                fos.flush();
            }
        }
    }

    // PERBAIKAN: Fungsi penulisan ditingkatkan untuk mempertahankan spasi antar paragraf & indentasi
    private static void writeTextToWord(XWPFDocument docxDocument, String text) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            XWPFParagraph paragraph = docxDocument.createParagraph();

            // Menghindari jarak antar baris yang terlalu renggang
            paragraph.setSpacingAfter(0);

            // Cek apakah ada spasi di awal baris (untuk mempertahankan indentasi)
            if (line.startsWith(" ")) {
                int spaceCount = 0;
                while (spaceCount < line.length() && line.charAt(spaceCount) == ' ') {
                    spaceCount++;
                }
                // 1 spasi kira-kira setara dengan 30 twips di Word
                paragraph.setIndentationLeft(spaceCount * 30);
            }

            // Mencegah baris kosong hilang begitu saja
            if (line.trim().isEmpty()) {
                paragraph.createRun().setText("");
                continue;
            }

            XWPFRun run = paragraph.createRun();
            run.setText(line.trim());
            run.setFontFamily("Calibri");
            run.setFontSize(11);
        }
    }
}
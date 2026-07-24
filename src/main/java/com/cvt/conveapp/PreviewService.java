package com.cvt.conveapp;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class PreviewService {

    /**
     * Menghasilkan Preview Image dari Berbagai Jenis File (Gambar, PDF, dan Word)
     */
    public static Image generatePreview(File file) throws Exception {
        String name = file.getName().toLowerCase();

        // 1. Preview File Gambar
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp")) {
            try (FileInputStream fis = new FileInputStream(file)) {
                return new Image(fis);
            }
        }
        // 2. Preview File PDF (Render Halaman Pertama)
        else if (name.endsWith(".pdf")) {
            try (PDDocument doc = Loader.loadPDF(file)) {
                if (doc.getNumberOfPages() > 0) {
                    PDFRenderer renderer = new PDFRenderer(doc);
                    BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 150);
                    return convertToFxImage(bufferedImage);
                }
            }
        }
        // 3. Preview File Word (.docx) -> Menggunakan Spire.Doc untuk hasil 100% presisi
        else if (name.endsWith(".docx")) {
            try {
                // Langkah A: Membaca dokumen Word menggunakan Spire.Doc
                com.spire.doc.Document document = new com.spire.doc.Document();
                document.loadFromFile(file.getAbsolutePath());

                // Langkah B: Merender halaman pertama (indeks 0) menjadi gambar Bitmap
                BufferedImage bufferedImage = document.saveToImages(0, com.spire.doc.documents.ImageType.Bitmap);

                // Langkah C: Membersihkan memori untuk mencegah kebocoran RAM
                document.dispose();

                // Langkah D: Mengonversi BufferedImage menjadi format gambar JavaFX agar bisa tampil di layar
                if (bufferedImage != null) {
                    return convertToFxImage(bufferedImage);
                }
            } catch (Exception e) {
                System.out.println("Gagal memuat preview Word: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // 4. Preview File Excel (.xlsx / .xls) -> Menggunakan Spire.Office
        else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            try {
                // Langkah A: Membuat objek Workbook dan memuat file Excel
                com.spire.xls.Workbook workbook = new com.spire.xls.Workbook();
                workbook.loadFromFile(file.getAbsolutePath());

                // Langkah B: Mengambil lembar kerja (sheet) pertama (indeks ke-0)
                com.spire.xls.Worksheet sheet = workbook.getWorksheets().get(0);

                // Langkah C: Membatasi area preview (maksimal 20 baris x 10 kolom) agar prosesnya ringan dan memori tidak penuh
                int lastRow = Math.min(sheet.getLastRow(), 20);
                int lastCol = Math.min(sheet.getLastColumn(), 10);

                // Pengecekan keamanan: Pastikan setidaknya ada 1 sel yang dirender meskipun sheet kosong
                if (lastRow < 1) lastRow = 1;
                if (lastCol < 1) lastCol = 1;

                // Langkah D: Memotret rentang sel tersebut dan mengubahnya menjadi gambar Bitmap
                BufferedImage bufferedImage = sheet.saveToImage(1, 1, lastRow, lastCol);

                // Langkah E: Membersihkan dokumen dari memori setelah selesai
                workbook.dispose();

                // Langkah F: Mengubah gambar ke format JavaFX (WritableImage) agar bisa tampil di UI
                if (bufferedImage != null) {
                    return convertToFxImage(bufferedImage);
                }
            } catch (Exception e) {
                System.out.println("Gagal memuat preview Excel: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Convert BufferedImage ke JavaFX Image tanpa memerlukan dependency external
     */
    private static WritableImage convertToFxImage(BufferedImage bufferedImage) {
        WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                pixelWriter.setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return writableImage;
    }
}
package com.cvt.conveapp;

import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import java.io.File;

/**
 * Layanan untuk mengonversi dokumen Word ke PDF dengan akurasi tata letak 100%.
 * Memanfaatkan Free Spire.Doc for Java.
 */
public class SpireWordToPdfService {

    /**
     * Metode utama konversi. Dilengkapi validasi maksimal 3 halaman.
     *
     * @param inputDocx File Word (.docx) yang akan dikonversi.
     * @param outputPdf Lokasi penyimpanan hasil PDF.
     * @throws Exception Jika halaman melebihi batas atau gagal memproses file.
     */
    public static void convertToPdf(File inputDocx, File outputPdf) throws Exception {
        // Inisialisasi objek dokumen Spire
        Document document = new Document();

        // Memuat file Word dari komputer
        document.loadFromFile(inputDocx.getAbsolutePath());

        // MENGHITUNG JUMLAH HALAMAN (Fitur Pengaman)
        int pageCount = document.getPageCount();

        if (pageCount > 3) {
            // Bersihkan memori dari dokumen ini
            document.dispose();
            // Lempar error ke Controller agar dimunculkan sebagai pop-up di layar
            throw new Exception("Gagal memproses: Dokumen memiliki " + pageCount +
                    " halaman. Versi gratis ini maksimal mendukung 3 halaman.");
        }

        // Menyimpan file sebagai PDF
        document.saveToFile(outputPdf.getAbsolutePath(), FileFormat.PDF);

        // Membersihkan memori sistem setelah selesai
        document.dispose();
    }
}
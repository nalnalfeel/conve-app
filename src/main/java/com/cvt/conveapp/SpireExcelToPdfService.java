package com.cvt.conveapp;

import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;
import java.io.File;

/**
 * Layanan untuk mengonversi dokumen Excel ke PDF.
 */
public class SpireExcelToPdfService {

    /**
     * Mengonversi dokumen Excel (.xlsx / .xls) ke PDF (.pdf).
     *
     * @param inputExcel File Excel yang akan diproses
     * @param outputPdf  Lokasi file PDF hasil konversi
     */
    public static void convertToPdf(File inputExcel, File outputPdf) {
        // 1. Membuat wadah Workbook baru
        Workbook workbook = new Workbook();

        // 2. Memuat file Excel dari komputer
        workbook.loadFromFile(inputExcel.getAbsolutePath());

        // 3. Mengatur agar lembar kerja (sheet) menyesuaikan ukuran halaman PDF (Opsional, agar tabel tidak terpotong)
        workbook.getConverterSetting().setSheetFitToPage(true);

        // 4. Menyimpan file tersebut langsung menjadi format PDF
        workbook.saveToFile(outputPdf.getAbsolutePath(), FileFormat.PDF);

        // 5. Membersihkan memori
        workbook.dispose();
    }
}
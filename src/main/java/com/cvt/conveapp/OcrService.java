package com.cvt.conveapp;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;

public class OcrService {

    private static Tesseract getTesseractInstance() throws Exception {
        Tesseract tesseract = new Tesseract();

        // 1. Arahkan ke folder tessdata di root proyek
        File tessDataFolder = new File("tessdata");

        if (!tessDataFolder.exists() || !tessDataFolder.isDirectory()) {
            tessDataFolder.mkdirs();
            throw new Exception("Folder 'tessdata' tidak ditemukan! Silakan letakkan file ind.traineddata / eng.traineddata di folder: " + tessDataFolder.getAbsolutePath());
        }

        boolean hasInd = new File(tessDataFolder, "ind.traineddata").exists();
        boolean hasEng = new File(tessDataFolder, "eng.traineddata").exists();

        if (!hasInd && !hasEng) {
            throw new Exception("Berkas model bahasa (.traineddata) tidak ditemukan di folder " + tessDataFolder.getAbsolutePath() + "!\nHarap unduh ind.traineddata atau eng.traineddata.");
        }

        tesseract.setDatapath(tessDataFolder.getAbsolutePath());

        // 2. Set bahasa yang tersedia
        if (hasInd && hasEng) {
            tesseract.setLanguage("ind+eng");
        } else if (hasInd) {
            tesseract.setLanguage("ind");
        } else {
            tesseract.setLanguage("eng");
        }

        return tesseract;
    }

    public static String doOcr(File imageFile) throws Exception {
        Tesseract tesseract = getTesseractInstance();
        return tesseract.doOCR(imageFile);
    }

    public static String doOcr(BufferedImage image) throws Exception {
        Tesseract tesseract = getTesseractInstance();
        return tesseract.doOCR(image);
    }
}
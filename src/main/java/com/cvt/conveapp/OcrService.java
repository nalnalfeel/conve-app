package com.cvt.conveapp;

import net.sourceforge.tess4j.Tesseract;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class OcrService {

    private static final String[] TRAINED_DATA_FILES = {"ind.traineddata", "eng.traineddata"};

    private static Tesseract getTesseractInstance() throws Exception {
        File tessDataFolder = resolveTessDataFolder();
        boolean hasInd = new File(tessDataFolder, "ind.traineddata").exists();
        boolean hasEng = new File(tessDataFolder, "eng.traineddata").exists();

        if (!hasInd && !hasEng) {
            throw new Exception("Berkas model bahasa (.traineddata) tidak ditemukan di folder " + tessDataFolder.getAbsolutePath() + "!\nHarap unduh ind.traineddata atau eng.traineddata.");
        }

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessDataFolder.getAbsolutePath());

        if (hasInd && hasEng) {
            tesseract.setLanguage("ind+eng");
        } else if (hasInd) {
            tesseract.setLanguage("ind");
        } else {
            tesseract.setLanguage("eng");
        }

        return tesseract;
    }

    private static File resolveTessDataFolder() throws IOException, URISyntaxException {
        List<Path> candidates = List.of(
                Paths.get(System.getProperty("user.dir"), "tessdata"),
                Paths.get(System.getProperty("user.home"), ".conve-app", "tessdata"),
                Paths.get(System.getProperty("java.io.tmpdir"), "conve-app", "tessdata")
        );

        for (Path candidate : candidates) {
            if (containsTrainedData(candidate)) {
                return candidate.toFile();
            }
        }

        URL resourceRoot = OcrService.class.getResource("/tessdata");
        if (resourceRoot != null) {
            Path extractedDir = Paths.get(System.getProperty("java.io.tmpdir"), "conve-app", "tessdata");
            extractBundledTessData(extractedDir);
            if (containsTrainedData(extractedDir)) {
                return extractedDir.toFile();
            }

            try {
                Path resourcePath = Paths.get(resourceRoot.toURI());
                if (Files.isDirectory(resourcePath) && containsTrainedData(resourcePath)) {
                    return resourcePath.toFile();
                }
            } catch (Exception ignored) {
                // ignore and continue to final error message
            }
        }

        throw new IOException("Folder 'tessdata' tidak ditemukan. Pastikan file ind.traineddata / eng.traineddata ada di folder aplikasi atau didalam paket executable.");
    }

    private static boolean containsTrainedData(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return false;
        }
        if (!Files.isDirectory(dir)) {
            return false;
        }
        return java.util.Arrays.stream(TRAINED_DATA_FILES)
                .anyMatch(fileName -> Files.exists(dir.resolve(fileName)) || Files.exists(dir.resolve(fileName.toLowerCase())));
    }

    private static void extractBundledTessData(Path targetDir) throws IOException {
        Files.createDirectories(targetDir);

        for (String fileName : TRAINED_DATA_FILES) {
            Path targetFile = targetDir.resolve(fileName);
            if (Files.exists(targetFile)) {
                continue;
            }

            try (InputStream in = OcrService.class.getResourceAsStream("/tessdata/" + fileName)) {
                if (in != null) {
                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
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
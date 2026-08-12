package com.cvt.conveapp;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.FileInputStream;
import org.apache.poi.xwpf.usermodel.Document;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private ComboBox<String> conversionTypeBox;
    @FXML private VBox dropArea;
    @FXML private Label fileLabel;
    @FXML private Button convertButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    /*@FXML private ImageView previewImageView;
    @FXML private Label previewInfoLabel;*/
    @FXML private javafx.scene.image.ImageView previewImageView;
    @FXML private Label previewInfoLabel;

    private List<File> selectedFiles = new ArrayList<>();


    @FXML
    public void initialize() {
        // Menambahkan opsi OCR Baru ke UI Dropdown
        conversionTypeBox.getItems().addAll(
                "Word ke PDF (.docx -> .pdf)",
                "PDF ke Word (.pdf -> .docx)",
                "Excel ke Word (.xlsx -> .docx)",
                "Word ke Excel (.docx -> .xlsx)",
                "Excel ke PDF (.xlsx -> .pdf)",
                "Gambar ke PDF (.jpg/.png -> .pdf)",
                "PDF ke Gambar (.pdf -> .jpg)",
                "Gambar ke Word (.jpg/.png -> .docx)",
                "Gambar ke Word OCR (.jpg/.png -> .docx)", // Opsi OCR Baru
                "Hapus Background Foto (.jpg/.png -> .png)"
        );
        conversionTypeBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Satu atau Beberapa Berkas");
        List<File> files = fileChooser.showOpenMultipleDialog(dropArea.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            setFiles(files);
        }
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            setFiles(db.getFiles());
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void setFiles(List<File> files) {
        List<File> extractedFiles = new ArrayList<>();

        for (File file : files){
            if (file.isDirectory()){
                File[] dirFiles = file.listFiles();
                if (dirFiles != null){
                    for (File f : dirFiles){
                        if (f.isFile()){
                            extractedFiles.add(f);
                        }
                    }
                }
            }else {
                extractedFiles.add(file);
            }
        }

        this.selectedFiles = extractedFiles;

        if (this.selectedFiles.isEmpty()){
            fileLabel.setText("Tarik & Lepas Berkas/Folder di sini");
            clearPreview("Tidak ada berkas valid yang ditemukan.");
        }else if (this.selectedFiles.size() == 1){
            File file = this.selectedFiles.get(0);
            fileLabel.setText("1 Berkas dipilih: " + file.getName());
            updatePreview(file);
        }else {
            fileLabel.setText(this.selectedFiles.size() + " Berkas Dipilih untuk Konversi Sekaligus");
            clearPreview("Preview tidak tersedia untuk banyak berkas sekaligus.");
        }
        /*this.selectedFiles = files;
        if (files.size() == 1) {
            File file = files.get(0);
            fileLabel.setText("1 Berkas Dipilih: " + file.getName());

            // Perintah ini yang akan memicu gambar preview muncul di kanan
            updatePreview(file);
        } else {
            fileLabel.setText(files.size() + " Berkas Dipilih untuk Konversi Sekaligus");

            // Kosongkan preview jika pengguna memilih banyak file
            clearPreview("Preview tidak tersedia untuk banyak berkas sekaligus.");
        }*/
    }

    @FXML
    private void handleConvert() {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            statusLabel.setText("⚠️ Pilih minimal satu berkas terlebih dahulu!");
            return;
        }

        String mode = conversionTypeBox.getValue();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Pilih Folder Tujuan Penyimpanan");
        File outputDir = directoryChooser.showDialog(dropArea.getScene().getWindow());

        if (outputDir == null) return;

        progressBar.setVisible(true);
        progressBar.setProgress(0);
        convertButton.setDisable(true);

        Task<Void> batchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int total = selectedFiles.size();

                for (int i = 0; i < total; i++) {
                    File file = selectedFiles.get(i);
                    String baseName = getFileNameWithoutExtension(file);
                    String ext = getOutputExtension(mode);

                    updateMessage("Memproses (" + (i + 1) + "/" + total + "): " + file.getName());
                    updateProgress(i, total);

                    if (mode.contains("PDF ke Gambar")) {
                        File subDir = new File(outputDir, baseName + "_images");
                        if (!subDir.exists()) subDir.mkdirs();
                        ImagePdfConverter.pdfToImage(file, subDir, "jpg");
                    } else {
                        File outputFile = new File(outputDir, baseName + ext);
                        executeSingleConversion(mode, file, outputFile);
                    }
                }

                updateProgress(total, total);
                return null;
            }
        };

        progressBar.progressProperty().bind(batchTask.progressProperty());
        statusLabel.textProperty().bind(batchTask.messageProperty());

        batchTask.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();

            progressBar.setVisible(false);
            convertButton.setDisable(false);
            statusLabel.setText("✅ Konversi / OCR Berhasil!");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Selesai");
            alert.setHeaderText(null);
            alert.setContentText("Semua berkas (" + selectedFiles.size() + " file) berhasil diproses dan disimpan ke:\n" + outputDir.getAbsolutePath());
            alert.showAndWait();
        });

        batchTask.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            statusLabel.textProperty().unbind();

            progressBar.setVisible(false);
            convertButton.setDisable(false);
            Throwable ex = batchTask.getException();
            statusLabel.setText("❌ Terjadi Kesalahan!");
            if (ex != null) ex.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Konversi / OCR");
            alert.setHeaderText("Gagal Mengonversi Berkas");
            alert.setContentText("Pesan Error: " + (ex != null ? ex.getMessage() : "Unknown error"));
            alert.showAndWait();
        });

        new Thread(batchTask).start();
    }

    /**
     * Memperbarui tampilan preview di layar kanan berdasarkan file yang dipilih.
     */
    private void updatePreview(File file) {
        try {
            // Meminta PreviewService untuk membaca dan mengubah halaman pertama menjadi gambar
            javafx.scene.image.Image previewImage = PreviewService.generatePreview(file);

            if (previewImage != null) {
                previewImageView.setImage(previewImage);
                previewInfoLabel.setText("Preview: " + file.getName());
            } else {
                clearPreview("Preview tidak didukung untuk format ini.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            clearPreview("Gagal memuat preview: " + e.getMessage());
        }
    }

    /**
     * Menghapus gambar preview dan menampilkan pesan teks.
     */
    private void clearPreview(String message) {
        if (previewImageView != null && previewInfoLabel != null) {
            previewImageView.setImage(null);
            previewInfoLabel.setText(message);
        }
    }


    private void executeSingleConversion(String mode, File inputFile, File outputFile) throws Exception {
        switch (mode) {
            case "Word ke PDF (.docx -> .pdf)":
                SpireWordToPdfService.convertToPdf(inputFile, outputFile);
                break;
            case "PDF ke Word (.pdf -> .docx)":
                WordPdfConverter.pdfToWord(inputFile, outputFile);
                break;
            case "Excel ke PDF (.xlsx -> .pdf)":
                SpireExcelToPdfService.convertToPdf(inputFile, outputFile);
                break;
            case "Excel ke Word (.xlsx -> .docx)":
                OfficeTableConverter.excelToWOrd(inputFile, outputFile);
                break;
            case "Word ke Excel (.docx -> .xlsx)":
                OfficeTableConverter.wordToExcel(inputFile, outputFile);
                break;
            case "Gambar ke PDF (.jpg/.png -> .pdf)":
                ImagePdfConverter.imageToPdf(inputFile, outputFile);
                break;
            case "Hapus Background Foto (.jpg/.png -> .png)":
                BackgroundRemoverService.removeBackground(inputFile, outputFile);
                break;

            // --- TAMBAHAN BARU: Mode Gambar ke Word (Menyisipkan Foto Fisik) ---
            case "Gambar ke Word (.jpg/.png -> .docx)":
                try (XWPFDocument doc = new XWPFDocument();
                     FileInputStream fis = new FileInputStream(inputFile);
                     FileOutputStream fos = new FileOutputStream(outputFile)) {

                    XWPFParagraph paragraph = doc.createParagraph();
                    XWPFRun run = paragraph.createRun();

                    // Deteksi tipe gambar
                    int format;
                    String fileName = inputFile.getName().toLowerCase();
                    if (fileName.endsWith(".png")) {
                        format = Document.PICTURE_TYPE_PNG;
                    } else {
                        format = Document.PICTURE_TYPE_JPEG;
                    }

                    // Ambil dimensi asli untuk menjaga rasio gambar
                    java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(inputFile);
                    int originalWidth = bimg != null ? bimg.getWidth() : 500;
                    int originalHeight = bimg != null ? bimg.getHeight() : 500;

                    // Hitung skala agar pas di halaman A4
                    int targetWidth = 460;
                    int targetHeight = (int) ((double) originalHeight / originalWidth * targetWidth);

                    // Sisipkan gambar ke dokumen Word
                    run.addPicture(
                            fis,
                            format,
                            inputFile.getName(),
                            org.apache.poi.util.Units.toEMU(targetWidth),
                            org.apache.poi.util.Units.toEMU(targetHeight)
                    );

                    doc.write(fos);
                }
                break;
            // --- BATAS TAMBAHAN BARU ---

            case "Gambar ke Word OCR (.jpg/.png -> .docx)":
                // Ekstraksi Teks Gambar via OCR -> Simpan ke Word .docx
                String ocrText = OcrService.doOcr(inputFile);
                try (XWPFDocument doc = new XWPFDocument();
                     FileOutputStream fos = new FileOutputStream(outputFile)) {
                    String[] lines = ocrText.split("\\r?\\n");
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            XWPFParagraph p = doc.createParagraph();
                            XWPFRun r = p.createRun();
                            r.setText(line);
                            r.setFontFamily("Calibri");
                            r.setFontSize(11);
                        }
                    }
                    doc.write(fos);
                }
                break;

        }
    }

    private String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex > 0) ? name.substring(0, lastIndex) : name;
    }

    private String getOutputExtension(String mode) {
        if (mode.contains("-> .pdf")) return ".pdf";
        if (mode.contains("-> .docx")) return ".docx";
        if (mode.contains("-> .xlsx")) return ".xlsx";
        if (mode.contains("-> .png")) return ".png";
        if (mode.contains("-> .jpg")) return ".jpg";
        return ".bin";
    }
}
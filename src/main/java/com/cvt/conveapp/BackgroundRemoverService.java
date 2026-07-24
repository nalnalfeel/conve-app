package com.cvt.conveapp;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Layanan untuk menghapus latar belakang gambar menggunakan Remove.bg API.
 */
public class BackgroundRemoverService {

    // TODO: Ganti teks di bawah ini dengan API Key milikmu dari remove.bg
    private static final String API_KEY = "DQHgy4HWBCF2xecSAVxawCJ8";

    /**
     * Memproses gambar dan mengembalikan gambar PNG transparan.
     *
     * @param inputFile  Gambar asli (.jpg, .png)
     * @param outputFile Gambar hasil (wajib berakhiran .png agar transparan)
     * @throws Exception Jika terjadi kesalahan koneksi atau kuota habis
     */
    public static void removeBackground(File inputFile, File outputFile) throws Exception {

        // 1. Baca gambar fisik dan ubah menjadi teks (Base64) agar mudah dikirim
        byte[] fileContent = Files.readAllBytes(inputFile.toPath());
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        // 2. Siapkan format instruksi (JSON) yang diminta oleh server
        String jsonBody = "{"
                + "\"image_file_b64\": \"" + encodedString + "\","
                + "\"size\": \"auto\""
                + "}";

        // 3. Bangun jalur komunikasi ke server menggunakan alat bawaan Java
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.remove.bg/v1.0/removebg"))
                .header("X-Api-Key", API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "image/png") // Meminta server mengembalikan gambar PNG
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 4. Kirim permintaan dan tunggu hasilnya
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // 5. Cek apakah berhasil (Kode 200 = Sukses)
        if (response.statusCode() == 200) {
            // Tulis serpihan data (byte) yang diterima menjadi gambar fisik di komputermu
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(response.body());
            }
        } else {
            // Jika error, ambil pesan kegagalannya (misal: API tidak valid atau kuota habis)
            String errorMsg = new String(response.body());
            throw new Exception("Gagal menghapus background. Kode: " + response.statusCode() + " - " + errorMsg);
        }
    }
}
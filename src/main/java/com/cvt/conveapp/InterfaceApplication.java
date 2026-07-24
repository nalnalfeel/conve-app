package com.cvt.conveapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class InterfaceApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(InterfaceApplication.class.getResource("hello-view.fxml"));
        // Ukuran jendela yang sudah kita sesuaikan sebelumnya
        Scene scene = new Scene(fxmlLoader.load(), 1050, 650);

        // --- TAMBAHAN KODE LOGO ---
        try {
            // Mengambil file logo.png dari folder resources
            Image appIcon = new Image(InterfaceApplication.class.getResourceAsStream("logo-conve.png"));
            // Memasang gambar sebagai ikon aplikasi
            stage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("Logo tidak ditemukan: " + e.getMessage());
        }
        // --- BATAS TAMBAHAN KODE LOGO ---

        stage.setTitle("Conve-App - Converter Document & Image");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

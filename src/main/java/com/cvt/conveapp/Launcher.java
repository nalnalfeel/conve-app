package com.cvt.conveapp;

import javafx.application.Application;

/**
 * Kelas launcher tunggal untuk menjalankan aplikasi JavaFX.
 * InterfaceApplication tidak lagi dipakai sebagai entry point langsung.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(InterfaceApplication.class, args);
    }
}

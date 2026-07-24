package com.cvt.conveapp;

/**
 * Kelas perantara murni agar JavaFX bisa dibungkus menjadi Fat JAR dan .exe dengan aman.
 */
public class Launcher {
    public static void main(String[] args) {
        // Memanggil fungsi utama dari aplikasi antarmuka kita
        InterfaceApplication.main(args);
    }
}

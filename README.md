Conve-App adalah aplikasi desktop JavaFX untuk mengonversi dokumen Word, Excel, PDF, dan gambar.

Cara build aplikasi:
- Pastikan JDK 21 terinstall.
- Jalankan: .\mvnw.cmd -DskipTests package

Cara membuat file .exe Windows:
- Install WiX Toolset 3.x atau lebih dari https://wixtoolset.org
- Setelah itu jalankan: .\package-exe.ps1

Catatan penting:
- OCR membutuhkan file model Tesseract di folder tessdata yang akan otomatis disalin ke JAR saat build.
- Aplikasi sudah dirancang agar data model bahasa tetap tersedia saat dijalankan dari file executable hasil bundling.

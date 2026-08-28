package com.cvt.conveapp;

import javafx.scene.image.Image;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;

public class ImagePdfConverter {
    public static void imageToPdf(File imageFile, File outputPdf) throws Exception{
        try (PDDocument doc = new PDDocument()){
            BufferedImage sourceImage = ImageIO.read(imageFile);
            if (sourceImage == null) {
                throw new IllegalArgumentException("File gambar tidak valid: " + imageFile.getAbsolutePath());
            }

            PDPage page = createPageForImage(sourceImage);
            doc.addPage(page);

            PDImageXObject image = PDImageXObject.createFromFileByContent(imageFile, doc);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)){
                content.drawImage(image, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            }
            doc.save(outputPdf);
        }
    }

    public static void pdfToImage(File pdfFile, File outputDir, String format) throws Exception{
        try(PDDocument doc = Loader.loadPDF(pdfFile)){
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); i++){
                BufferedImage bim = renderer.renderImageWithDPI(i, 300);
                File outputFile = new File(outputDir,"halaman" + (i + 1) + "." + format.toLowerCase());
                ImageIO.write(bim, format, outputFile);
            }
        }
    }

    public static void imagesToPdf(java.util.List<File> imageFiles, File outputPdf) throws Exception{
        try(PDDocument doc = new PDDocument()) {
            for (File imageFile : imageFiles){
                BufferedImage sourceImage = ImageIO.read(imageFile);
                if (sourceImage == null) {
                    throw new IllegalArgumentException("File gambar tidak valid: " + imageFile.getAbsolutePath());
                }

                PDPage page = createPageForImage(sourceImage);
                doc.addPage(page);

                PDImageXObject image = PDImageXObject.createFromFileByContent(imageFile, doc);
                try (PDPageContentStream content = new PDPageContentStream(doc, page)){
                    content.drawImage(image, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                }
            }
            doc.save(outputPdf);
        }
    }

    private static PDPage createPageForImage(BufferedImage image) {
        float width = Math.max(image.getWidth(), 1);
        float height = Math.max(image.getHeight(), 1);
        return new PDPage(new PDRectangle(width, height));
    }
}

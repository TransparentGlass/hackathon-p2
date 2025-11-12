package com.team.translator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.tess4j.Tesseract;

public class OcrService {
    private final Tesseract tesseract;

    /**
     * Create OcrService.
     * tessdataPath: optional path. If null or empty, TESSDATA_PREFIX env var will be used.
     * The constructor will try to locate the language traineddata file before initializing Tess4J
     * and will throw an IllegalStateException with guidance if not found.
     */
    public OcrService(String tessdataPath, String language) {
        String base = tessdataPath;
        if (base == null || base.isEmpty()) {
            base = System.getenv("TESSDATA_PREFIX");
            if (base == null || base.isEmpty()) base = System.getenv("TESSDATA");
        }

        List<String> candidates = new ArrayList<>();
        if (base != null && !base.isEmpty()) {
            candidates.add(base);
            candidates.add(base + File.separator + "tessdata");
        }
        // also try common Windows install location
        candidates.add("C:" + File.separator + "Program Files" + File.separator + "Tesseract-OCR");
        candidates.add("C:" + File.separator + "Program Files (x86)" + File.separator + "Tesseract-OCR");

        String found = null;
        String trained = language + ".traineddata";
        for (String c : candidates) {
            if (c == null) continue;
            File f1 = new File(c, trained);
            File f2 = new File(c + File.separator + "tessdata", trained);
            if (f1.exists()) { found = c; break; }
            if (f2.exists()) { found = c + File.separator + "tessdata"; break; }
        }

        if (found == null) {
            StringBuilder tried = new StringBuilder();
            for (String s : candidates) {
                if (s == null) continue;
                tried.append(s).append("; ");
            }
            throw new IllegalStateException("Tesseract language data not found. Tried locations: " + tried.toString()
                    + "\nPlease install Tesseract and set the TESSDATA_PREFIX environment variable or pass the correct path to OcrService."
                    + "\nExample tessdata path (Windows): C\\\\Program Files\\\\Tesseract-OCR\\\\tessdata");
        }

        tesseract = new Tesseract();
        tesseract.setDatapath(found);
        tesseract.setLanguage(language);
    }

    public String read(BufferedImage image) throws Exception {
        try {
            return tesseract.doOCR(image);
        } catch (Throwable t) {
            // Wrap native/JNA errors into an Exception so caller can show a friendly message
            throw new Exception("OCR failed: " + t.getMessage(), t);
        }
    }
}
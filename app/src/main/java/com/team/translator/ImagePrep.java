package com.team.translator;

import java.awt.image.BufferedImage;

public class ImagePrep {

    public static BufferedImage toHighContrast(BufferedImage image, int threshold) {
        // Convert to grayscale (basic implementation)
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage grayImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        java.awt.Graphics2D g2 = grayImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        // If threshold <= 0, compute a simple automatic threshold (mean luminance)
        if (threshold <= 0) {
            long sum = 0;
            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    int c = grayImage.getRGB(xx, yy) & 0xff;
                    sum += c;
                }
            }
            threshold = (int) (sum / (w * (long) h));
        }

        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int color = grayImage.getRGB(xx, yy);
                int gray = color & 0xff;
                gray = (gray > threshold) ? 255 : 0; // Basic thresholding
                int rgb = (gray << 16) | (gray << 8) | gray;
                grayImage.setRGB(xx, yy, rgb);
            }
        }
        return grayImage;
    }

    public static BufferedImage scale2x(BufferedImage image) {
        // Upsample image by 2x
        int newWidth = image.getWidth() * 2;
        int newHeight = image.getHeight() * 2;
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        scaledImage.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
        return scaledImage;
    }


}
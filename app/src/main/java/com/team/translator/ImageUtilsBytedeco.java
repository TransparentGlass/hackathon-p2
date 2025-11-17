package com.team.translator;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class ImageUtilsBytedeco {

    // PUBLIC API — Call this directly
    public static BufferedImage process(BufferedImage bi){
        Mat processed = preprocessImage(bi);
        return matToBufferedImage(processed);
    }

    // ------------------------------- //
    //    BUFFEREDIMAGE → BYTE IMAGE   //
    // ------------------------------- //

    // Fixes DataBufferInt/ClassCastException
    public static BufferedImage toByteImage(BufferedImage bi) {
        if (bi.getType() == BufferedImage.TYPE_3BYTE_BGR ||
            bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return bi; // already safe
        }

        // Convert ANY BufferedImage to 3BYTE_BGR
        BufferedImage converted = new BufferedImage(
                bi.getWidth(),
                bi.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR
        );

        Graphics2D g = converted.createGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return converted;
    }

    // ------------------------------- //
    //     BYTE IMAGE → MAT (BYTED)    //
    // ------------------------------- //

    public static Mat bufferedImageToMat(BufferedImage bi) {
        bi = toByteImage(bi); // ensure byte-backed

        int width = bi.getWidth();
        int height = bi.getHeight();

        Mat mat;
        if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            mat = new Mat(height, width, CV_8UC1);
        } else {
            mat = new Mat(height, width, CV_8UC3); // BGR
        }

        byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        BytePointer ptr = new BytePointer(mat.data());
        ptr.put(pixels);

        return mat;
    }

    // ------------------------------- //
    //       IMAGE PREPROCESSING        //
    // ------------------------------- //

    public static Mat preprocessImage(BufferedImage bi) {
    Mat mat = bufferedImageToMat(bi);

    // 1. Gray
    Mat gray = new Mat();
    cvtColor(mat, gray, COLOR_BGR2GRAY);

    // 2. Light blur
    Mat blur = new Mat();
    GaussianBlur(gray, blur, new Size(3, 3), 0);

    // 3. Remove glow + sharpen edges (unsharp mask)
    Mat sharpen = new Mat();
    Mat temp = new Mat();
    GaussianBlur(blur, temp, new Size(9, 9), 10);
    addWeighted(blur, 1.8, temp, -0.8, 0, sharpen);

    // 4. Soft adaptive threshold (not too harsh)
    Mat thresh = new Mat();
    adaptiveThreshold(
        sharpen,
        thresh,
        255,
        ADAPTIVE_THRESH_GAUSSIAN_C,
        THRESH_BINARY,
        25,  // smaller block = softer
        7    // lower C = softer
    );

    return thresh;
}



    // ------------------------------- //
    //       MAT → BUFFEREDIMAGE        //
    // ------------------------------- //

    public static BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();

        BufferedImage image =
                (channels == 1)
                        ? new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
                        : new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        BytePointer source = new BytePointer(mat.data());

        source.get(targetPixels); // fast byte copy

        return image;
    }
}

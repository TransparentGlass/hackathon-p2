package com.team.translator;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class Screencapture {
    
        private static Robot robot;

        static {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }

    public static BufferedImage capture(int x, int y, int w, int h) {
        Rectangle screenRect = new Rectangle(x, y, w, h);
        return robot.createScreenCapture(screenRect);
    }
}

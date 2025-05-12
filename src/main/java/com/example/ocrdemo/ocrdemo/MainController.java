package com.example.ocrdemo.ocrdemo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class MainController {
    @FXML
    private Label resultText;
    @FXML
    private Button demo1;
    @FXML
    private Button demo2;
    @FXML
    private Button demo3;
    @FXML
    private Button demo4;

    private static void ProcessVideo() throws FrameGrabber.Exception {
        String videoPath = "path/to/your/video.mp4";

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/TessData");
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        Frame frame;
        int frameCount = 0;
        while ((frame = grabber.grabImage()) != null && frameCount < 300) {
            Mat mat = converter.convert(frame);
            if (mat == null) continue;

            Mat gray = new Mat();
            cvtColor(mat, gray, COLOR_BGR2GRAY);

            Mat thresh = new Mat();
            threshold(gray, thresh, 0, 255, THRESH_BINARY | THRESH_OTSU);

            BufferedImage buffered = new Java2DFrameConverter().convert(converter.convert(thresh));

            try {
                String result = tesseract.doOCR(buffered);
                if (!result.trim().isEmpty()) {
                    System.out.println("Detected text on frame " + frameCount + ":\n" + result.trim());
                }
            } catch (TesseractException e) {
                System.err.println("OCR failed on frame " + frameCount + ": " + e.getMessage());
            }

            frameCount++;
        }

        grabber.stop();
        grabber.close();
    }
}
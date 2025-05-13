package com.example.ocrdemo.ocrdemo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.util.regex.*;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class MainController {
    @FXML
    private Label resultText;

    @FXML
    public void initialize() {
        resultText.setText("Please select a demo");
    }

    @FXML
    private void handleDemo1() {
        runDemo("Test1.mp4");
    }

    @FXML
    private void handleDemo2() {
        try {
            String result = ProcessVideo("Test2.mp4");
            resultText.setText(result.isEmpty() ? "No text detected" : result);
        } catch (Exception e) {
            resultText.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void refineDemo2() {
        String pattern = "([A-Z][A-Z][A-Z][0-9][0-9][0-9])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(resultText.getText());
        if (m.find()) {
            resultText.setText(m.group(1));
        }
    }

    @FXML
    private void handleDemo3() {
        runDemo("Test3.mp4");
    }

    @FXML
    private void handleDemo4() {
        runDemo("Test4.mp4");
    }

    private void runDemo(String filename) {
        try {
            String result = ProcessVideo(filename);
            resultText.setText(result.isEmpty() ? "No text detected" : result);
        } catch (Exception e) {
            resultText.setText("Error: " + e.getMessage());
        }
    }

    private String ProcessVideo(String vidName) throws FrameGrabber.Exception {
        String videoPath = "src/main/resources/TestVideos/" + vidName;

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter java2DConverter = new Java2DFrameConverter();

        // Much can be done to improve accuracy, easiest to hardest:
        // limiting chars to a-z + 1-9, cropping to expected area, and improving processing method
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/TessData");
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        Frame frame;
        while ((frame = grabber.grabImage()) != null) {
            Mat mat = converter.convert(frame);
            if (mat == null) continue;

            Mat gray = new Mat();
            cvtColor(mat, gray, COLOR_BGR2GRAY);
            Mat thresh = new Mat();
            threshold(gray, thresh, 0, 255, THRESH_BINARY | THRESH_OTSU);
            BufferedImage buffered = java2DConverter.convert(converter.convert(thresh));
            try {
                String result = tesseract.doOCR(buffered).trim();
                if (!result.isEmpty()) {
                    grabber.stop();
                    grabber.close();
                    return result;
                }
            } catch (TesseractException e) {
                resultText.setText("OCR failed!");
            }
        }

        grabber.stop();
        grabber.close();
        return ""; // return empty string if no text found
    }
}
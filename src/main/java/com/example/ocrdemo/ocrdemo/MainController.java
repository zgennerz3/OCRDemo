package com.example.ocrdemo.ocrdemo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
        runDemo("Test1.mp4", "[A-Z][A-Z][0-9][0-9][A-Z][A-Z]");
    }

    @FXML
    private void handleDemo2() {
        runDemo("Test2.mp4", "[0-9][0-9][0-9][A-Z][A-Z][A-Z]");
    }

    @FXML
    private void handleDemo3() {
        runDemo("Test3.mp4", "[0-9][0-9][0-9][A-Z][A-Z][A-Z]");
    }

    @FXML
    private void handleDemo4() {
        runDemo("Test4.mp4", "[0-9][0-9][0-9][A-Z][A-Z][A-Z]");
    }

    @FXML
    private void handleDemo5() {
        runDemo("Test5.mp4", "[A-Z][A-Z][0-9][0-9][A-Z][A-Z]");
    }

    @FXML
    private MediaView video;
    private MediaPlayer mediaPlayer;


    private void playVideo(String filename){
        String videoPath = "src/main/resources/TestVideos/" + filename;
        if (mediaPlayer != null){
            mediaPlayer.stop();
        }

        Media media = new Media(new File(videoPath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setMute(true);
        video.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
    }

    private void runDemo(String filename, String plateRegex) {
        try {
            playVideo(filename);
            String result = ProcessVideo(filename, plateRegex);
            resultText.setText(result.isEmpty() ? "No text detected" : result);
        } catch (Exception e) {
            resultText.setText("Error: " + e.getMessage());
        }
    }

    private String ProcessVideo(String vidName, String plateRegex) throws FrameGrabber.Exception {
        String videoPath = "src/main/resources/TestVideos/" + vidName;
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter java2DConverter = new Java2DFrameConverter();

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/TessData");
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        Pattern plateFormat = Pattern.compile(plateRegex);
        Map<String, Integer> plateCounts = new HashMap<>();
        int frameCount = 0;
        int frameSkip = 6;

        Frame frame;
        while ((frame = grabber.grabImage()) != null) {
            if (frameCount % frameSkip != 0) {
                frameCount++; continue; }
            Mat mat = converter.convert(frame);
            if (mat == null) {
                frameCount++; continue; }

            Mat gray = new Mat();
            cvtColor(mat, gray, COLOR_BGR2GRAY);
            Mat thresh = new Mat();
            threshold(gray, thresh, 0, 255, THRESH_BINARY | THRESH_OTSU);

            BufferedImage buffered = java2DConverter.convert(converter.convert(thresh));
            try {
                String result = tesseract.doOCR(buffered).trim();
                if (!result.isEmpty()) {
                    Matcher matcher = plateFormat.matcher(result);
                    while (matcher.find()) {
                        String plate = matcher.group();
                        plateCounts.put(plate, plateCounts.getOrDefault(plate, 0) + 1);
                    }
                }
            } catch (TesseractException e) {
                resultText.setText("OCR failed!");
            }
            frameCount++;
        }

        grabber.stop();
        grabber.close();
        System.out.println("Detected plates: " + plateCounts);

        if (!plateCounts.isEmpty()) {
            return plateCounts.entrySet().stream().max(Map.Entry.comparingByValue())
                    .get().getKey();
        }
        return ""; // return empty string if no text found
    }
}
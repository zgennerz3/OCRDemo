package com.example.ocrdemo.ocrdemo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_QUIET;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;
import org.bytedeco.javacv.FFmpegLogCallback;


public class OCRDemoApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(OCRDemoApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 720, 480);
        stage.setTitle("OCRDemo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        FFmpegLogCallback.set();           // Required for log redirection
        av_log_set_level(AV_LOG_QUIET);
        launch();
    }
}
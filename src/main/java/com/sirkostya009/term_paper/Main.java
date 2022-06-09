package com.sirkostya009.term_paper;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage = new World("Gaem", 640, 480);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.sirkostya009.termpaper;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setScene(World.INSTANCE);
        stage.setResizable(false);
        stage.setTitle("Gaem");
        stage.show();
    }
}
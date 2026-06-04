package com.railway;

import com.railway.engine.StationMaster;
import com.railway.engine.TrainVirtualThread;
import com.railway.model.Train;
import com.railway.ui.MapRenderer;
import com.railway.model.RailwayNetwork;
import com.railway.config.MapConfig;
import com.railway.config.TrainConfig;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RailwayApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        RailwayNetwork network = MapConfig.createMap();
        TrainConfig.setupTrains(network);
        StationMaster stationMaster = new StationMaster();

        Canvas canvas = new Canvas(800, 600);
        MapRenderer renderer = new MapRenderer(canvas, network);

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Railway Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Rendering loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderer.draw();
            }
        };
        timer.start();

        // Start Trains
        for (Train train : network.trains) {
            if (train.getRoute() != null && train.getRoute().segments() != null) {
                Thread.ofVirtual().start(new TrainVirtualThread(train, stationMaster));
            }
        }
    }

    static void main(String[] args) {
        launch(args);
    }
}

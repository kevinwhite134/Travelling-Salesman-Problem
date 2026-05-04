package org.example;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.controller.TSPController;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        TSPController controller = new TSPController();

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1100, bounds.getWidth() * 0.95);
        double height = Math.min(650, bounds.getHeight() * 0.90);

        Scene scene = new Scene(controller.getRoot(), width, height);
        stage.setTitle("TSP Visualiser");
        stage.setScene(scene);
        stage.show();

        controller.init(); // generate first set of points
    }

    public static void main(String[] args) {
        launch();
    }
}

package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main {

    public void start(Stage primaryStage) throws Exception {
        Label label = new Label("Hello World!");
        VBox layout = new VBox(10, label);
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
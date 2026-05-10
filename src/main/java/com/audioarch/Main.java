package com.audioarch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    // ejecucion del programa

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 720);
        scene.setFill(Color.TRANSPARENT);

        try {
            Image icono = new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png"));
            if (icono.isError()) {
                System.err.println("Error: No se pudo cargar la imagen del icono. Revisa la ruta.");
            } else {
                stage.getIcons().add(icono);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el icono: " + e.getMessage());
        }

        stage.setTitle("AudioArch");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        
        // Apply rounded corners clip that updates on resize
        applyRoundedCorners(scene, root, 18, stage);
        
        com.audioarch.util.ResizeHelper.addResizeListener(stage);
        
        stage.setMaximized(true); // Fullscreen effect 
        stage.show();
    }

    /**
     * Applies a rounded rectangle clip to the root node that updates dynamically on resize.
     */
    private void applyRoundedCorners(Scene scene, Parent root, double radius, Stage stage) {
        javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle();
        clipRect.setArcWidth(radius * 2);
        clipRect.setArcHeight(radius * 2);
        
        // Bind clip size to scene size
        clipRect.widthProperty().bind(scene.widthProperty());
        clipRect.heightProperty().bind(scene.heightProperty());
        
        root.setClip(clipRect);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.audioarch.ui.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneTransitionUtil {

    public static void cambiarVistaConBlur(Stage stage, Parent nuevoRoot) {
        // En lugar de setScene, conservamos el root original para no cambiar dimensiones del stage
        if (stage.getScene() == null) {
            stage.setScene(new Scene(nuevoRoot));
        }

        // Aplica el nuevo root
        stage.getScene().setRoot(nuevoRoot);
        com.audioarch.util.ResizeHelper.addResizeListener(stage);

        // Re-apply rounded corners clip
        applyRoundedCorners(stage.getScene(), nuevoRoot, 18, stage);

        // Configura el efecto de animacion blur
        GaussianBlur blur = new GaussianBlur(25);
        nuevoRoot.setEffect(blur);

        // Animacion del Blur de 25 a 0
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 25)),
                new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 0))
        );
        timeline.setOnFinished(e -> nuevoRoot.setEffect(null)); // Quita el efecto al finalizar
        timeline.play();
    }

    private static void applyRoundedCorners(Scene scene, Parent root, double radius, Stage stage) {
        javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle();
        
        clipRect.setArcWidth(radius * 2);
        clipRect.setArcHeight(radius * 2);
        
        clipRect.widthProperty().bind(scene.widthProperty());
        clipRect.heightProperty().bind(scene.heightProperty());
        
        root.setClip(clipRect);
    }
}


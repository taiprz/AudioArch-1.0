package com.audioarch.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TitleBarController {

    @FXML private HBox titleBar;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            if (stage != null && !stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                maximizar(new ActionEvent(titleBar, null));
            }
        });
    }

    @FXML
    private void minimizar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    private javafx.geometry.Rectangle2D backupBounds;
    private boolean isMaximizedManual = false;

    @FXML
    private void maximizar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        if (isMaximizedManual) {
            if (backupBounds != null) {
                stage.setX(backupBounds.getMinX());
                stage.setY(backupBounds.getMinY());
                stage.setWidth(backupBounds.getWidth());
                stage.setHeight(backupBounds.getHeight());
            }
            isMaximizedManual = false;
        } else {
            backupBounds = new javafx.geometry.Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            javafx.collections.ObservableList<javafx.stage.Screen> screens = javafx.stage.Screen.getScreensForRectangle(
                    new javafx.geometry.Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
            javafx.stage.Screen screen = screens.isEmpty() ? javafx.stage.Screen.getPrimary() : screens.get(0);
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            isMaximizedManual = true;
        }
    }

    @FXML
    private void cerrar(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}

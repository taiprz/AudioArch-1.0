package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class CustomAlertController {
    @FXML private Label lblIcon;
    @FXML private Label lblTitle;
    @FXML private Label lblMessage;
    @FXML private StackPane iconContainer;

    @FXML
    public void initialize() {
        // Garantiza el centrado por código además del FXML
        lblTitle.setTextAlignment(TextAlignment.CENTER);
        lblTitle.setAlignment(Pos.CENTER);
        lblMessage.setTextAlignment(TextAlignment.CENTER);
        lblMessage.setAlignment(Pos.CENTER);
        lblIcon.setTextAlignment(TextAlignment.CENTER);
        lblIcon.setAlignment(Pos.CENTER);
    }

    public void setAlert(String title, String message, AlertType type) {
        lblTitle.setText(title);
        lblMessage.setText(message);

        switch (type) {
            case SUCCESS:
                lblIcon.setText("✓");
                lblIcon.setStyle("-fx-text-fill: #00e676; -fx-font-size: 38; -fx-font-weight: bold;");
                break;
            case ERROR:
                lblIcon.setText("✕");
                lblIcon.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 38; -fx-font-weight: bold;");
                break;
            case INFO:
            default:
                lblIcon.setText("ℹ");
                lblIcon.setStyle("-fx-text-fill: #fe7faa; -fx-font-size: 38; -fx-font-weight: bold;");
                break;
        }
    }

    @FXML
    private void aceptar() {
        ((Stage) lblTitle.getScene().getWindow()).close();
    }

    public enum AlertType {
        SUCCESS, ERROR, INFO
    }
}

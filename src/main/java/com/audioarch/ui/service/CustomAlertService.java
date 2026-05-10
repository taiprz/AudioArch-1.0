package com.audioarch.ui.service;

import com.audioarch.ui.controller.CustomAlertController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlertService {

    public static void show(String title, String message, CustomAlertController.AlertType type) {
        try {
            FXMLLoader loader = new FXMLLoader(CustomAlertService.class.getResource("/fxml/custom_alert.fxml"));
            Parent root = loader.load();
            
            CustomAlertController controller = loader.getController();
            controller.setAlert(title, message, type);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("Error al mostrar la alerta: " + e.getMessage());
            // Fallback to standard alert if custom fails
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}

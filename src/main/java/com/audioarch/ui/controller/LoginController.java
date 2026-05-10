package com.audioarch.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.audioarch.repository.UsuarioDao;
import com.audioarch.domain.model.Usuario;
import com.audioarch.ui.controller.CustomAlertController;
import com.audioarch.ui.service.CustomAlertService;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;

    private double xOffset = 0;
    private double yOffset = 0;


    // metodo para iniciar sesion
    @FXML
    private void login(ActionEvent event) {
        String user = txtUsuario.getText().trim();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Completa todos los campos");
            return;
        }

        try {
            Usuario usuario = UsuarioDao.login(user, pass);

            if (usuario != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                    Parent root = loader.load();

                    DashboardController dashController = loader.getController();
                    dashController.setUsuario(usuario);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    SceneTransitionUtil.cambiarVistaConBlur(stage, root);

                } catch (IOException e) {
                    CustomAlertService.show("Algo ha ido mal", "No hemos podido abrir la app. Por favor, cierra y vuelve a intentarlo.", CustomAlertController.AlertType.ERROR);
                }
            } else {
                lblMensaje.setText("Usuario o contraseña incorrectos");
                lblMensaje.setStyle("-fx-text-fill: #ff3333;");
            }
        } catch (Exception e) {
            CustomAlertService.show("Sin servicio", "Parece que hay un problema con el servicio en este momento. Inténtalo de nuevo en unos segundos.", CustomAlertController.AlertType.ERROR);
        }
    }

    // metodo para el boton de cerrar
    @FXML
    private void cerrarApp(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    // metodo para cargar la vista del registro
    @FXML
    public void handleRegistro(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registro.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            SceneTransitionUtil.cambiarVistaConBlur(stage, root);
        } catch (IOException e) {
            CustomAlertService.show("Algo ha ido mal", "No hemos podido cargar la pantalla de registro.", CustomAlertController.AlertType.ERROR);
        }
    }

    // configura el arrastre de ventana (se llama desde el FXML)
    public void inicializarArrastre(Parent root, Stage stage) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}

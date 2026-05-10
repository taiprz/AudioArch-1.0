package com.audioarch.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.audioarch.repository.UsuarioDao;
import com.audioarch.domain.model.Usuario;
import com.audioarch.ui.controller.CustomAlertController;
import com.audioarch.ui.service.CustomAlertService;

import java.io.IOException;

public class RegistroController {

    @FXML private TextField txtNuevoUsuario, txtEmail;
    @FXML private PasswordField txtPassword, txtConfirmarPassword;
    @FXML private CheckBox checkTerminos;
    @FXML private Label lblMensaje;

    private static final int MIN_PASSWORD_LENGTH = 6;
    // TLD mínimo de 3 chars para evitar typos como .co en lugar de .com
    private static final String EMAIL_REGEX = "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{3,}$";

    @FXML
    private void registrarUsuario() {
        String usuario  = txtNuevoUsuario.getText().trim();
        String email    = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirm  = txtConfirmarPassword.getText();

        // 1. Campos vacíos
        if (usuario.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Por favor, rellena todos los campos.");
            return;
        }

        // 2. Formato de email
        if (!email.matches(EMAIL_REGEX)) {
            lblMensaje.setText("Introduce un correo electrónico válido.");
            return;
        }

        // 3. Longitud mínima de contraseña
        if (password.length() < MIN_PASSWORD_LENGTH) {
            lblMensaje.setText("La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres.");
            return;
        }

        // 4. Confirmación de contraseña
        if (!password.equals(confirm)) {
            lblMensaje.setText("Las contraseñas no coinciden.");
            return;
        }

        // 5. Términos y condiciones
        if (!checkTerminos.isSelected()) {
            lblMensaje.setText("Debes aceptar los términos y condiciones.");
            return;
        }

        // 6. Usuario ya en uso
        if (UsuarioDao.existeUsuario(usuario)) {
            lblMensaje.setText("Ese nombre de usuario ya está en uso. Prueba con otro.");
            return;
        }

        // 7. Email ya registrado
        if (UsuarioDao.existeEmail(email)) {
            lblMensaje.setText("Ese correo ya está registrado. ¿Ya tienes cuenta?");
            return;
        }

        // Crear y guardar
        Usuario nuevo = new Usuario();
        nuevo.setUser(usuario);
        nuevo.setEmail(email);
        nuevo.setClave(password);

        try {
            UsuarioDao.InsertarUsuario(nuevo);
            CustomAlertService.show("¿Listo para escuchar?", "¡Tu cuenta está creada! Ahora puedes iniciar sesión y unirte a la comunidad.", CustomAlertController.AlertType.SUCCESS);
            Stage stage = (Stage) txtNuevoUsuario.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            SceneTransitionUtil.cambiarVistaConBlur(stage, root);
        } catch (Exception e) {
            CustomAlertService.show("No ha podido ser", "No hemos podido crear tu cuenta ahora mismo. Inténtalo de nuevo en unos instantes.", CustomAlertController.AlertType.ERROR);
        }
    }

    // volver al login
    @FXML
    private void volverLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneTransitionUtil.cambiarVistaConBlur(stage, root);
    }
}
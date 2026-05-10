package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.audioarch.domain.model.Usuario;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

public class ItemUsuarioController {
    @FXML private ImageView imgFotoPerfil;
    @FXML private Label lblUsername, lblBio;
    @FXML private Button btnVerPerfil;

    private Usuario usuarioCard;
    private Usuario usuarioSesion;
    private DashboardController parentDashboard;

    public void setData(Usuario usuarioCard, Usuario usuarioSesion) {
        setData(usuarioCard, usuarioSesion, null);
    }

    public void setData(Usuario usuarioCard, Usuario usuarioSesion, DashboardController parent) {
        this.usuarioCard = usuarioCard;
        this.usuarioSesion = usuarioSesion;
        this.parentDashboard = parent;

        lblUsername.setText(usuarioCard.getUser());
        
        if (usuarioCard.getBiografia() != null && !usuarioCard.getBiografia().isBlank()) {
            // Trim bio if too long for card
            String bio = usuarioCard.getBiografia();
            if (bio.length() > 60) bio = bio.substring(0, 57) + "...";
            lblBio.setText(bio);
        } else {
            lblBio.setText("Sin biografía");
        }

        cargarImagenLocal(usuarioCard.getFotoPerfil());
    }

    private void cargarImagenLocal(String ruta) {
        if (ruta != null && !ruta.isEmpty()) {
            try {
                if (ruta.startsWith("data:image")) {
                    // Imagen embebida como Base64
                    String base64 = ruta.substring(ruta.indexOf(",") + 1);
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    imgFotoPerfil.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else if (ruta.startsWith("http")) {
                    // URL externa
                    imgFotoPerfil.setImage(new Image(ruta, true));
                } else {
                    // Recurso interno del classpath
                    String pathFormateado = ruta.startsWith("/") ? ruta : "/" + ruta;
                    var resource = getClass().getResourceAsStream(pathFormateado);
                    if (resource != null) imgFotoPerfil.setImage(new Image(resource));
                }
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen de perfil: " + e.getMessage());
            }
        }
    }

    @FXML
    private void abrirPerfil() {
        if (parentDashboard != null) {
            parentDashboard.abrirPerfilUsuario(usuarioCard);
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/perfil.fxml"));
            javafx.scene.Parent root = loader.load();
            PerfilController controller = loader.getController();
            controller.setDatos(usuarioCard, usuarioSesion);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            // Optionally undecorated for custom close button handling
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            CustomAlertService.show(
                "Error",
                "No hemos podido cargar el perfil del usuario.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }
}

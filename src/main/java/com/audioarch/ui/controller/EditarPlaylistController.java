package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.audioarch.repository.PlaylistDao;
import com.audioarch.domain.model.Playlist;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

public class EditarPlaylistController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtFoto;
    @FXML private TextArea txtDescripcion;
    @FXML private ImageView imgPreview;

    private Playlist playlist;
    private PlaylistViewController parentController;

    public void initData(Playlist playlist, PlaylistViewController parentController) {
        this.playlist = playlist;
        this.parentController = parentController;

        txtNombre.setText(playlist.getNombre());
        txtDescripcion.setText(playlist.getDescripcion());
        
        txtFoto.setText("");
        txtFoto.setPromptText(playlist.getPortadaUrl() != null && !playlist.getPortadaUrl().isBlank() ? "Portada actual cargada. Déjalo en blanco para mantenerla." : "Ruta de la portada...");

        cargarImagen(imgPreview, playlist.getPortadaUrl());

        txtFoto.textProperty().addListener((observable, oldValue, newValue) -> {
            cargarImagen(imgPreview, newValue != null && !newValue.isBlank() ? newValue : playlist.getPortadaUrl());
        });
    }

    private void cargarImagen(ImageView imageView, String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            try {
                if (ruta.startsWith("data:image")) {
                    String base64 = ruta.substring(ruta.indexOf(",") + 1);
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    imageView.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else if (ruta.startsWith("http") || ruta.startsWith("file:")) {
                    imageView.setImage(new Image(ruta, true));
                } else {
                    String pathFormateado = ruta.startsWith("/") ? ruta : "/" + ruta;
                    var resource = getClass().getResourceAsStream(pathFormateado);
                    if (resource != null) {
                        imageView.setImage(new Image(resource));
                    } else {
                        imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                    }
                }
            } catch (Exception e) {}
        } else {
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
            } catch(Exception e) {}
        }
    }

    @FXML
    private void seleccionarArchivo() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Portada de la Playlist");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg", "*.gif")
        );
        java.io.File file = fileChooser.showOpenDialog(txtNombre.getScene().getWindow());
        if (file != null) {
            String dataUri = convertirArchivoABase64(file);
            if (dataUri != null) txtFoto.setText(dataUri);
        }
    }

    private String convertirArchivoABase64(java.io.File file) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String ext = file.getName().toLowerCase();
            String mime = ext.endsWith(".png") ? "image/png" : ext.endsWith(".gif") ? "image/gif" : "image/jpeg";
            return "data:" + mime + ";base64," + base64;
        } catch (Exception e) {
            CustomAlertService.show(
                "Error al leer la imagen",
                "No se pudo procesar el archivo seleccionado.",
                CustomAlertController.AlertType.ERROR
            );
            return null;
        }
    }

    @FXML
    private void guardar() {
        String nuevoNombre = txtNombre.getText().trim();
        
        if (nuevoNombre.isEmpty()) {
            CustomAlertService.show("Campo obligatorio",
                "El nombre de la playlist no puede estar vacío.",
                CustomAlertController.AlertType.ERROR);
            return;
        }
        
        if (nuevoNombre.length() > 60) {
            CustomAlertService.show("Nombre demasiado largo",
                "El nombre de la playlist no puede superar los 60 caracteres.",
                CustomAlertController.AlertType.ERROR);
            return;
        }

        playlist.setNombre(nuevoNombre);
        String newFoto = txtFoto.getText().trim();
        if (!newFoto.isEmpty()) {
            playlist.setPortadaUrl(newFoto);
        }
        playlist.setDescripcion(txtDescripcion.getText().trim());

        PlaylistDao.actualizar(playlist);

        if (parentController != null) {
            parentController.refrescarUI(); // force track and header view refresh
        }

        javafx.application.Platform.runLater(() -> {
            CustomAlertService.show(
                "Éxito", 
                "Playlist actualizada correctamente.", 
                CustomAlertController.AlertType.SUCCESS
            );
        });

        cerrar();
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}

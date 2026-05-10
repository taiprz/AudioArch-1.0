package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.audioarch.repository.UsuarioDao;
import com.audioarch.domain.model.Usuario;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

public class EditarPerfilController {
    @FXML private TextField txtUser;
    @FXML private TextField txtFoto;
    @FXML private TextField txtBanner;
    @FXML private TextArea txtBio;
    @FXML private ImageView imgPreview;
    @FXML private ImageView imgBannerPreview;

    private Usuario usuario;
    private PerfilController parentController;

    public void setDatos(Usuario usuario, PerfilController parentController) {
        this.usuario = usuario;
        this.parentController = parentController;

        txtUser.setText(usuario.getUser());
        txtBio.setText(usuario.getBiografia());
        
        txtFoto.setText("");
        txtBanner.setText("");
        
        txtFoto.setPromptText(usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isBlank() ? "Imagen actual cargada. Déjalo en blanco para mantenerla." : "Ruta de la imagen de perfil...");
        txtBanner.setPromptText(usuario.getBanner() != null && !usuario.getBanner().isBlank() ? "Banner actual cargado. Déjalo en blanco para mantenerlo." : "Ruta del banner...");

        cargarImagen(imgPreview, usuario.getFotoPerfil());
        cargarImagen(imgBannerPreview, usuario.getBanner());

        // Actualizar preview dinámicamente si se cambia la URL, o volver a la actual si se borra
        txtFoto.textProperty().addListener((observable, oldValue, newValue) -> {
            cargarImagen(imgPreview, newValue != null && !newValue.isBlank() ? newValue : usuario.getFotoPerfil());
        });
        txtBanner.textProperty().addListener((observable, oldValue, newValue) -> {
            cargarImagen(imgBannerPreview, newValue != null && !newValue.isBlank() ? newValue : usuario.getBanner());
        });
    }

    private void cargarImagen(ImageView imageView, String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            try {
                if (ruta.startsWith("data:image")) {
                    // Imagen embebida como Base64
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
            } catch (Exception e) {
                try {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                } catch(Exception ex) {}
            }
        } else {
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
            } catch(Exception e) {}
        }
    }

    @FXML
    private void seleccionarArchivo() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Perfil");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg", "*.gif")
        );
        java.io.File file = fileChooser.showOpenDialog(txtUser.getScene().getWindow());
        if (file != null) {
            String dataUri = convertirArchivoABase64(file);
            if (dataUri != null) {
                txtFoto.setText(dataUri);
            }
        }
    }

    @FXML
    private void seleccionarArchivoBanner() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Banner");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg", "*.gif")
        );
        java.io.File file = fileChooser.showOpenDialog(txtUser.getScene().getWindow());
        if (file != null) {
            String dataUri = convertirArchivoABase64(file);
            if (dataUri != null) {
                txtBanner.setText(dataUri);
            }
        }
    }

    /**
     * Convierte un archivo de imagen a una cadena Base64 (data URI).
     * Esto permite guardar la imagen directamente en la BD y que
     * todos los usuarios puedan verla sin depender de rutas locales.
     */
    private String convertirArchivoABase64(java.io.File file) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String extension = file.getName().toLowerCase();
            String mime;
            if (extension.endsWith(".png"))       mime = "image/png";
            else if (extension.endsWith(".gif"))  mime = "image/gif";
            else                                  mime = "image/jpeg";
            return "data:" + mime + ";base64," + base64;
        } catch (Exception e) {
            CustomAlertService.show(
                "Error al leer la imagen",
                "No se pudo procesar el archivo seleccionado. Prueba con otro formato.",
                CustomAlertController.AlertType.ERROR
            );
            return null;
        }
    }

    @FXML
    private void guardar() {
        String nuevoUser = txtUser.getText().trim();
        String newFoto   = txtFoto.getText();
        String newBanner = txtBanner.getText();
        String newBio    = txtBio.getText();

        // Validaciones
        if (nuevoUser.isEmpty()) {
            CustomAlertService.show("Campo obligatorio",
                "El nombre de usuario no puede estar vacío.",
                CustomAlertController.AlertType.ERROR);
            return;
        }
        if (nuevoUser.length() < 3) {
            CustomAlertService.show("Usuario demasiado corto",
                "El nombre de usuario debe tener al menos 3 caracteres.",
                CustomAlertController.AlertType.ERROR);
            return;
        }
        // Solo bloquear si el nombre cambio Y ya existe en otro usuario
        if (!nuevoUser.equalsIgnoreCase(usuario.getUser()) && UsuarioDao.existeUsuario(nuevoUser)) {
            CustomAlertService.show("Usuario ocupado",
                "Ese nombre de usuario ya lo está usando otra persona.",
                CustomAlertController.AlertType.ERROR);
            return;
        }
        if (newBio != null && newBio.length() > 300) {
            CustomAlertService.show("Biografía muy larga",
                "La biografía no puede superar los 300 caracteres.",
                CustomAlertController.AlertType.ERROR);
            return;
        }

        usuario.setUser(nuevoUser);
        usuario.setBiografia(newBio);
        if (!newFoto.isEmpty()) {
            usuario.setFotoPerfil(newFoto);
        }
        if (!newBanner.isEmpty()) {
            usuario.setBanner(newBanner);
        }

        try {
            UsuarioDao.actualizarUsuario(usuario);

            // Actualizar la sesión global para que los cambios se reflejen en toda la app
            if (ItemController.getUsuarioSesion() != null && ItemController.getUsuarioSesion().getId() == usuario.getId()) {
                ItemController.setUsuarioSesion(usuario);
                if (DashboardController.getInstance() != null) {
                    // Refrescar solo el nombre en la barra lateral sin reiniciar el dashboard
                    DashboardController.getInstance().actualizarSesion(usuario);
                }
            }

            if (parentController != null) {
                parentController.setDatos(usuario, usuario);
            }
            
            CustomAlertService.show(
                "¡Todo listo!", 
                "Tu perfil se ha actualizado correctamente.", 
                CustomAlertController.AlertType.SUCCESS
            );
            cerrar();
        } catch (Exception e) {
            CustomAlertService.show(
                "No ha podido ser", 
                "No hemos podido guardar tus cambios ahora mismo. Inténtalo de nuevo en unos instantes.", 
                CustomAlertController.AlertType.ERROR
            );
        }
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtUser.getScene().getWindow();
        stage.close();
    }
}

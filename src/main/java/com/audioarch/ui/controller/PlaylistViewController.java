package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.audioarch.domain.model.Playlist;
import com.audioarch.domain.model.ItemPlaylist;
import com.audioarch.repository.PlaylistDao;
import javafx.geometry.Pos;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

public class PlaylistViewController {
    @FXML private ImageView imgPortada;
    @FXML private Label lblNombre, lblDescripcion, lblMetadata;
    @FXML private VBox vboxTracks;
    @FXML private Button btnEditar;
    
    private Playlist playlist;
    private DashboardController parentDashboard;
    private boolean isOwner;

    public void initData(Playlist p, boolean isOwner, DashboardController parent) {
        this.playlist = p;
        this.isOwner = isOwner;
        this.parentDashboard = parent;
        
        btnEditar.setVisible(isOwner);

        refrescarUI();
    }

    public void refrescarUI() {
        lblNombre.setText(playlist.getNombre());
        lblDescripcion.setText(playlist.getDescripcion() != null && !playlist.getDescripcion().isBlank() ? playlist.getDescripcion() : "Sin descripción.");
        
        int totalSecs = playlist.getDuracionTotal();
        int mins = totalSecs / 60;
        int secs = totalSecs % 60;
        
        lblMetadata.setText("Por " + playlist.getUsuario().getUser() + " • " + playlist.getItems().size() + " temas • " + mins + " min " + secs + " s");

        cargarImagen(imgPortada, playlist.getPortadaUrl(), 160);
        
        vboxTracks.getChildren().clear();
        int index = 1;
        for (ItemPlaylist item : playlist.getItems()) {
            HBox row = new HBox(20);
            row.setStyle("-fx-padding: 10 25; -fx-alignment: CENTER_LEFT; -fx-background-radius: 12;");
            
            Label lblIndex = new Label(String.valueOf(index++));
            lblIndex.setStyle("-fx-text-fill: #fe7faa; -fx-min-width: 30; -fx-font-size: 14; -fx-font-weight: bold;");
            
            // Contenedor de la foto con overlay
            javafx.scene.layout.StackPane imgContainer = new javafx.scene.layout.StackPane();
            
            ImageView iv = new ImageView();
            iv.setFitWidth(50);
            iv.setFitHeight(50);
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(50, 50);
            clip.setArcWidth(15); clip.setArcHeight(15);
            iv.setClip(clip);
            cargarImagen(iv, item.getFotoUrl(), 50);
            
            Label lblPlay = new Label("▶");
            lblPlay.setStyle("-fx-text-fill: #fec7e8; -fx-font-size: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
            lblPlay.setVisible(false);
            
            javafx.scene.layout.Region overlay = new javafx.scene.layout.Region();
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 8;");
            overlay.setVisible(false);
            
            imgContainer.getChildren().addAll(iv, overlay, lblPlay);
            
            // Animaciones hover en la fila
            row.setOnMouseEntered(e -> {
                row.setStyle("-fx-padding: 10 25; -fx-alignment: CENTER_LEFT; -fx-background-color: rgba(254, 127, 170, 0.1); -fx-background-radius: 12; -fx-cursor: hand;");
                lblIndex.setVisible(false);
                if (item.getPreviewUrl() != null && !item.getPreviewUrl().isBlank()) {
                    lblPlay.setVisible(true);
                    overlay.setVisible(true);
                }
            });
            row.setOnMouseExited(e -> {
                row.setStyle("-fx-padding: 10 25; -fx-alignment: CENTER_LEFT; -fx-background-color: transparent; -fx-background-radius: 12;");
                lblIndex.setVisible(true);
                lblPlay.setVisible(false);
                overlay.setVisible(false);
            });
            
            // Clic para reproducir
            row.setOnMouseClicked(e -> {
                if (parentDashboard != null && item.getPreviewUrl() != null && !item.getPreviewUrl().isBlank()) {
                    parentDashboard.playPreview(item.getPreviewUrl(), item.getTitulo(), item.getArtista(), item.getFotoUrl());
                }
            });
            
            VBox vbText = new VBox(4);
            vbText.setAlignment(Pos.CENTER_LEFT);
            Label lTitle = new Label(item.getTitulo());
            lTitle.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: 800;");
            Label lArt = new Label(item.getArtista());
            lArt.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13; -fx-font-weight: 600;");
            vbText.getChildren().addAll(lTitle, lArt);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            int iS = item.getDuracion();
            Label lTime = new Label(String.format("%d:%02d", iS/60, iS%60));
            lTime.setStyle("-fx-text-fill: #aaa; -fx-min-width: 50; -fx-font-size: 14; -fx-font-weight: 600;");
            
            if (isOwner) {
                Button btnEliminarItem = new Button("✖");
                btnEliminarItem.setStyle("-fx-background-color: transparent; -fx-text-fill: #fe7faa; -fx-cursor: hand; -fx-font-size: 14; -fx-font-weight: bold;");
                btnEliminarItem.setOnAction(ev -> {
                    ev.consume();
                    playlist.getItems().remove(item);
                    PlaylistDao.actualizar(playlist);
                    refrescarUI();
                });
                row.getChildren().addAll(lblIndex, imgContainer, vbText, spacer, lTime, btnEliminarItem);
            } else {
                row.getChildren().addAll(lblIndex, imgContainer, vbText, spacer, lTime);
            }
            
            vboxTracks.getChildren().add(row);
        }
    }

    private void cargarImagen(ImageView iv, String url, int fallBackSize) {
        try {
            if (url != null && !url.isBlank()) {
                if (url.startsWith("data:image")) {
                    String base64 = url.substring(url.indexOf(",") + 1);
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    iv.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else if (url.startsWith("http") || url.startsWith("file:")) {
                    iv.setImage(new Image(url, true));
                } else {
                    String path = url.startsWith("/") ? url : "/" + url;
                    java.io.InputStream stream = getClass().getResourceAsStream(path);
                    if (stream != null) iv.setImage(new Image(stream));
                    else setDefault(iv);
                }
            } else {
                setDefault(iv);
            }
        } catch(Exception e) {
            setDefault(iv);
        }
    }
    
    private void setDefault(ImageView iv) {
        try {
            iv.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
        } catch(Exception e) {}
    }

    @FXML
    private void abrirEdicion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editar_playlist.fxml"));
            Parent root = loader.load();
            EditarPlaylistController controller = loader.getController();
            controller.initData(this.playlist, this);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch(Exception e) { 
            CustomAlertService.show(
                "Error",
                "No hemos podido cargar la ventana.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }

    @FXML
    private void cerrar() {
        if (parentDashboard != null) {
            parentDashboard.cerrarPlaylistOverlay();
        }
    }
}

package app.musicplayer.util;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Album;
import app.musicplayer.view.AlbumsController;
import javafx.animation.Animation;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class AlbumListCell<T extends ArrayList<Album>> extends ListCell<T> {

    private AlbumsController controller;
    private T item;

    public AlbumListCell(AlbumsController controller) {
        this.controller = controller;
        this.getStyleClass().setAll("album-list-cell");
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else if (!item.equals(this.item)) {
            this.item = item;
            HBox row = new HBox();
            item.forEach(album -> {
                VBox cell = createCell(album, item);
                row.getChildren().add(cell);
            });
            setGraphic(row);
        }
    }

    private VBox createCell(Album album, ArrayList<Album> row) {

        VBox cell = new VBox();
        Label title = new Label(album.getTitle());
        ImageView image = new ImageView(album.getArtwork());
        image.imageProperty().bind(album.artworkProperty());
        VBox imageBox = new VBox();

        title.setTextOverrun(OverrunStyle.CLIP);
        title.setWrapText(true);
        title.setPadding(new Insets(10, 0, 10, 0));
        title.setAlignment(Pos.TOP_LEFT);
        title.setPrefHeight(66);
        title.prefWidthProperty().bind(this.widthProperty().subtract(102).divide(5));

        image.fitWidthProperty().bind(this.widthProperty().subtract(102).divide(5));
        image.fitHeightProperty().bind(this.widthProperty().subtract(102).divide(5));
        image.setPreserveRatio(true);
        image.setSmooth(true);

        imageBox.prefWidthProperty().bind(this.widthProperty().subtract(102).divide(5));
        imageBox.prefHeightProperty().bind(this.widthProperty().subtract(102).divide(5));
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(image);

        cell.getChildren().addAll(imageBox, title);
        cell.setPadding(new Insets(10, 10, 0, 10));
        cell.getStyleClass().add("album-cell");
        cell.setAlignment(Pos.CENTER);
        cell.setOnMouseClicked(event -> {

            PseudoClass selected = PseudoClass.getPseudoClass("selected");
            Animation collapseAnimation = controller.getCollapseAnimation();
            Animation expandAnimation = controller.getExpandAnimation();
            Animation tableCollapseAnimation = controller.getTableCollapseAnimation();
            Animation tableExpandAnimation = controller.getTableExpandAnimation();

            // If the album detail is collapsed, expand it and populate song table.
            boolean isAlbumDetailCollapsed = controller.isAlbumDetailCollapsed();
            Album selectedAlbum = controller.getSelectedAlbum();
            VBox selectedCell = controller.getSelectedCell();

            if (isAlbumDetailCollapsed) {

                cell.pseudoClassStateChanged(selected, true);

                // Updates the currently selected cell.
                controller.selectAlbum(album, cell);

                // Shows song table, plays load animation and populates song table with album songs.
                controller.expandAlbumDetail();
                expandAnimation.play();

                controller.setHeader(album.getArtist(), album.getTitle());
                controller.populateSongTable(cell, album);

            // Else if album detail is expanded and opened album is reselected.
            } else if (!isAlbumDetailCollapsed && album.equals(selectedAlbum)) {

                cell.pseudoClassStateChanged(selected, false);
                controller.selectAlbum(null, null);

                // Plays the collapse animation to remove the song table.
                collapseAnimation.play();

            // Else if album detail is expanded and a different album is selected on the same row.
            } else if (!isAlbumDetailCollapsed && !album.equals(selectedAlbum)) {

                if (selectedCell != null) {
                    selectedCell.pseudoClassStateChanged(selected, false);
                }

                cell.pseudoClassStateChanged(selected, true);

                // Updates the currently selected cell.
                controller.selectAlbum(album, cell);

                // Plays load animation and populates song table with songs of newly selected album.
                tableCollapseAnimation.setOnFinished(x -> {
                    controller.setHeader(album.getArtist(), album.getTitle());
                    controller.populateSongTable(cell, album);
                    controller.expandAlbumDetail();
                    tableExpandAnimation.play();
                    tableCollapseAnimation.setOnFinished(y -> controller.collapseAlbumDetail());
                });

                tableCollapseAnimation.play();

            } else {

                if (selectedCell != null) {
                    selectedCell.pseudoClassStateChanged(selected, false);
                }

                controller.selectAlbum(null, null);

                // Plays the collapse animation to remove the song table.
                collapseAnimation.play();
            }
        });

        cell.setOnDragDetected(event -> {
            PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
            cell.pseudoClassStateChanged(pressed, false);
            Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString("Album");
            db.setContent(content);
            MusicPlayer.setDraggedItem(album);
            db.setDragView(cell.snapshot(null, null), cell.widthProperty().divide(2).get(), cell.heightProperty().divide(2).get());
            event.consume();
        });

        return cell;
    }
}
package app.musicplayer.util;

import app.musicplayer.MusicPlayer;
import app.musicplayer.view.MainController;
import app.musicplayer.view.ArtistsMainController;
import app.musicplayer.model.Library;
import app.musicplayer.model.Artist;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

public class ArtistListCell<T extends ArrayList<Artist>> extends ListCell<T> {

    private T item;

    public ArtistListCell() {
        this.getStyleClass().setAll("artist-list-cell");
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
            item.forEach(artist -> {
                VBox cell = createCell(artist);
                row.getChildren().add(cell);
            });
            setGraphic(row);
        }
    }

    private VBox createCell(Artist artist) {

        VBox cell = new VBox();
        Label title = new Label(artist.getTitle());
        ImageView image = new ImageView(artist.getArtistImage());
        image.imageProperty().bind(artist.artistImageProperty());
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
        cell.getStyleClass().add("artist-cell");
        cell.setAlignment(Pos.CENTER);
        cell.setOnMouseClicked(event -> {

            MainController mainController = MusicPlayer.getMainController();
            ArtistsMainController artistsMainController = (ArtistsMainController) mainController.loadView("ArtistsMain");
            String artistTitle = title.getText();
            Artist a = Library.getArtist(artistTitle);
            artistsMainController.selectArtist(a);
        });

        cell.setOnDragDetected(event -> {
            PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
            cell.pseudoClassStateChanged(pressed, false);
            Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString("Artist");
            db.setContent(content);
            MusicPlayer.setDraggedItem(artist);
            db.setDragView(cell.snapshot(null, null), cell.getWidth() / 2, cell.getHeight() / 2);
            event.consume();
        });

        return cell;
    }
}
package musicplayer;

import java.net.URL;
import java.util.Collections;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.application.Platform;

public class AlbumsController implements Initializable, Refreshable {
	
	@FXML private FlowPane flowPane;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<Album> albums = FXCollections.observableArrayList(Library.getAlbums());
		Collections.sort(albums);

        int limit = (albums.size() < 25) ? albums.size() : 25;

		for (int i = 0; i < limit; i++) {

            Album album = albums.get(i);
            flowPane.getChildren().add(createCell(album));
		}

        int rows = (albums.size() % 5 == 0) ? albums.size() / 5 : albums.size() / 5 + 1;
        flowPane.prefHeightProperty().bind(flowPane.widthProperty().divide(5).add(16).multiply(rows));

        new Thread(() -> {

            ArrayList<VBox> cells = new ArrayList<VBox>();

            for (int j = 25; j < albums.size(); j++) {
                Album album = albums.get(j);
                cells.add(createCell(album));
            }

            Platform.runLater(() -> {
                flowPane.getChildren().addAll(cells);
            });

        }).start();
	}

    @Override
    public void refresh() {

    }

    private VBox createCell(Album album) {

        VBox cell = new VBox();
        Label title = new Label(album.getTitle());
        ImageView image = new ImageView(album.getArtwork());
        VBox imageBox = new VBox();

        title.setTextOverrun(OverrunStyle.CLIP);
        title.setWrapText(true);
        title.setPadding(new Insets(10, 0, 10, 0));
        title.setAlignment(Pos.TOP_LEFT);
        title.setPrefHeight(66);
        title.prefWidthProperty().bind(flowPane.widthProperty().divide(5).subtract(21));

        image.fitWidthProperty().bind(flowPane.widthProperty().divide(5).subtract(21));
        image.fitHeightProperty().bind(flowPane.widthProperty().divide(5).subtract(21));
        image.setPreserveRatio(true);
        image.setSmooth(true);

        imageBox.prefWidthProperty().bind(flowPane.widthProperty().divide(5).subtract(21));
        imageBox.prefHeightProperty().bind(flowPane.widthProperty().divide(5).subtract(21));
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(image);

        cell.getChildren().addAll(imageBox, title);
        cell.setPadding(new Insets(10, 10, 10, 10));
        cell.getStyleClass().add("artist-cell");
        cell.setAlignment(Pos.CENTER);
        cell.setOnMouseClicked(event -> {

            VBox albumCell = (VBox) event.getSource();
            String albumTitle = ((Label) albumCell.getChildren().get(1)).getText();
            Album a = Library.getAlbums().stream().filter(x -> albumTitle.equals(x.getTitle())).findFirst().get();

        });

        return cell;
    }
}

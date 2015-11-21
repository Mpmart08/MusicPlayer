package musicplayer;

import java.net.URL;
import java.util.Collections;
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

public class AlbumsController implements Initializable {
	
	@FXML private ScrollPane scrollPane;
	@FXML private FlowPane flowPane;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<Album> albums = FXCollections.observableArrayList(Library.getAlbums());
		Collections.sort(albums);
		
		for (Album album: albums) {
            VBox cell = new VBox();
            Label title = new Label(album.getTitle());
            ImageView image = new ImageView(album.getArtwork());

            title.setTextOverrun(OverrunStyle.CLIP);
            title.setWrapText(true);
            title.setPrefHeight(50);
            title.prefWidthProperty().bind(flowPane.widthProperty().divide(4).subtract(52));

            image.fitWidthProperty().bind(flowPane.widthProperty().divide(4).subtract(52));
            image.fitHeightProperty().bind(flowPane.widthProperty().divide(4).subtract(52));
            image.setPreserveRatio(true);
            image.setSmooth(true);
            image.setCache(true);

            cell.getChildren().addAll(image, title);
            cell.setPadding(new Insets(10, 10, 0, 10));
            cell.getStyleClass().add("artist-cell");
            cell.setOnMouseClicked(event -> {

                VBox albumCell = (VBox) event.getSource();
                String albumTitle = ((Label) albumCell.getChildren().get(1)).getText();
                Album a = Library.getAlbums().stream().filter(x -> albumTitle.equals(x.getTitle())).findFirst().get();

            });

            flowPane.getChildren().add(cell);
            flowPane.setMargin(cell, new Insets(25, 25, 0, 0));
		}
		
		scrollPane.getStyleClass().add("scroll-pane");
		flowPane.getStyleClass().add("flow-pane");

        int rows = (albums.size() % 4 == 0) ? albums.size() / 4 : albums.size() / 4 + 1;
        flowPane.prefHeightProperty().bind(flowPane.widthProperty().divide(4).add(18).multiply(rows));
        
	}
}

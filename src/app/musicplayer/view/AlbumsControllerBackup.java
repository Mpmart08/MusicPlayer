package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.model.Album;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.Refreshable;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the albums view.
 *
 */
public class AlbumsControllerBackup implements Initializable, Refreshable {
	
	private boolean isAlbumDetailCollapsed = true;
    private double expandedHeight = 250;
    private double collapsedHeight = 50;
	
	@FXML private HBox box;
    @FXML private FlowPane grid;
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
	
    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> {setVisibility(true);});
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            grid.setPrefHeight(curHeight);
        }
    };
	
	private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1.0 - frac);
            grid.setPrefHeight(curHeight);
        }
    };
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<Album> albums = FXCollections.observableArrayList(Library.getAlbums());
		Collections.sort(albums);

        int limit = (albums.size() < 25) ? albums.size() : 25;

		for (int i = 0; i < limit; i++) {

            Album album = albums.get(i);
            grid.getChildren().add(createCell(album));
		}

        int rows = (albums.size() % 5 == 0) ? albums.size() / 5 : albums.size() / 5 + 1;
        grid.prefHeightProperty().bind(grid.widthProperty().divide(5).add(16).multiply(rows));

        new Thread(() -> {

            ArrayList<VBox> cells = new ArrayList<VBox>();

            for (int j = 25; j < albums.size(); j++) {
                Album album = albums.get(j);
                cells.add(createCell(album));
            }

            Platform.runLater(() -> {
                grid.getChildren().addAll(cells);
            });

        }).start();
	} // End initialize()

    @Override
    public void refresh() {}

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
        title.prefWidthProperty().bind(grid.widthProperty().divide(5).subtract(21));

        image.fitWidthProperty().bind(grid.widthProperty().divide(5).subtract(21));
        image.fitHeightProperty().bind(grid.widthProperty().divide(5).subtract(21));
        image.setPreserveRatio(true);
        image.setSmooth(true);

        imageBox.prefWidthProperty().bind(grid.widthProperty().divide(5).subtract(21));
        imageBox.prefHeightProperty().bind(grid.widthProperty().divide(5).subtract(21));
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(image);

        cell.getChildren().addAll(imageBox, title);
        cell.setPadding(new Insets(10, 10, 10, 10));
        cell.getStyleClass().add("artist-cell");
        cell.setAlignment(Pos.CENTER);
        cell.setOnMouseClicked(event -> {
        	// If the album detail is collapsed, expand it. Else collapse it.
        	if (isAlbumDetailCollapsed) {
        		// TODO: DEBUG
        		System.out.println("expand");
        		expandAlbumDetail();
        	} else {
        		// TODO: DEBUG
        		System.out.println("collapse");
        		collapseAlbumDetail();
        	}
        });
        return cell;
    } // End createCell()
    
    private void expandAlbumDetail() {
        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

            expandAnimation.play();
        }
    }
    
    private void collapseAlbumDetail() {
        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

            setVisibility(false);
            collapseAnimation.play();
        }
    }
    
    private void setVisibility(boolean isVisible) {
        for (Node n : grid.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setVisible(isVisible);
                    }
                }
            }
        }
    }
}

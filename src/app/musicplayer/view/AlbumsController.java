package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Album;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.Refreshable;
import javafx.animation.Animation;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AlbumsController implements Initializable, Refreshable {
	
	private boolean isAlbumDetailCollapsed = true;
	
    @FXML private FlowPane grid;
    @FXML private VBox songBox;
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
	
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
		// Sets song box margins so that it only covers a third of the flowpane when expanded.
//        songBox.setMargin(songTable, new Insets(grid.getPrefHeight()/3, 15, 10, 10));
		// Sets the song table to be invisible when the view is initialized.
        songBox.setVisible(false);
	}

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
        	
        	// TODO: DEBUG
        	System.out.println("Cell clicked!");
        	
        	// If the album detail is collapsed, expand it. Else collapse it.
        	if (isAlbumDetailCollapsed) {
        		expandAlbumDetail(cell);
        		populateSongTable(cell, album);
        	} else {
        		collapseAlbumDetail(cell);
        	}
        });
        
        titleColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        
        return cell;
    }
    
    private void expandAlbumDetail(VBox cell) {
    	// TODO: DEBUG
    	System.out.println("128: Expand Album Detail");
    	
    	// TODO: DEBUG
    	System.out.println(cell.getBoundsInParent().getMaxX());
    	System.out.println(cell.getBoundsInParent().getMaxY());
    	
    	songBox.setTranslateY(cell.getBoundsInParent().getMaxY()-20);
    	
    	isAlbumDetailCollapsed = false;
    	songBox.setVisible(true);
    	
    }
    
    private void collapseAlbumDetail(VBox cell) {
    	// TODO: DEBUG
    	System.out.println("135: Collapse Album Detail");
    	isAlbumDetailCollapsed = true;
    	songBox.setVisible(false);
    }
    
    private void populateSongTable(VBox cell, Album selectedAlbum) {
    	// TODO: DEBUG
    	System.out.println("Populate Song Table");
    	
    	System.out.println("Album Title: " + selectedAlbum.getTitle());
    	
    	ArrayList<Song> albumSongsAL = selectedAlbum.getSongs();
    	ObservableList<Song> albumSongs = FXCollections.observableArrayList(albumSongsAL);
    	
    	System.out.println("Album First Song (AL): " + albumSongsAL.get(0).getTitle());
    	System.out.println("Album First Song (OL): " + albumSongs.get(0).getTitle());
    	
        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));
        
        songTable.setItems(albumSongs);
    	
    }
}

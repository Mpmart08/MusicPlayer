package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.model.Album;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.Refreshable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
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
            grid.getChildren().add(createCell(album, i));
		}

        int rows = (albums.size() % 5 == 0) ? albums.size() / 5 : albums.size() / 5 + 1;
        grid.prefHeightProperty().bind(grid.widthProperty().divide(5).add(16).multiply(rows));

        new Thread(() -> {

            ArrayList<VBox> cells = new ArrayList<VBox>();

            for (int j = 25; j < albums.size(); j++) {
                Album album = albums.get(j);
                cells.add(createCell(album, j));
            }

            Platform.runLater(() -> {
                grid.getChildren().addAll(cells);
            });
        }).start();
        
        // Sets preferred column width.
        titleColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        
		// Sets song box margins so that they are in line with the album cover cells.
        songBox.setMargin(songTable, new Insets(0, 10, 0, 5));
        
		// Sets the song table to be invisible when the view is initialized.
        songBox.setVisible(false);
	}

    @Override
    public void refresh() {}

    private VBox createCell(Album album, int index) {

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
        		expandAlbumDetail(cell, index);
        		populateSongTable(cell, album);
        	} else {
        		collapseAlbumDetail(cell);
        	}
        });
        return cell;
    }
    
    private void expandAlbumDetail(VBox cell, int index) {
    	// TODO: DEBUG
    	System.out.println("128: Expand Album Detail");
    	System.out.println("Index: " + index);
    	
    	// Converts the index integer to a string.
    	String indexString = Integer.toString(index);
    	
    	// Initializes index used to insert song table into flowpane.
    	int insertIndex = 0;
    	
    	// Defines insertIndex based on the clicked cell index so that the song table
    	// is inserted in the row after the clicked row.
    	if (indexString.endsWith("0") || indexString.endsWith("5")) {
    		insertIndex = index + 5;
    	} else if (indexString.endsWith("1") || indexString.endsWith("6"))  {
    		insertIndex = index + 4;
    	} else if (indexString.endsWith("2") || indexString.endsWith("7"))  {
    		insertIndex = index + 3;
    	} else if (indexString.endsWith("3") || indexString.endsWith("8"))  {
    		insertIndex = index + 2;
    	} else if (indexString.endsWith("4") || indexString.endsWith("9"))  {
    		insertIndex = index + 1;
    	}
    	
    	// Adds the song box to the flow pane.
    	grid.getChildren().add(insertIndex, songBox);
    	isAlbumDetailCollapsed = false;
    	songBox.setVisible(true);
    	
    }
    
    private void collapseAlbumDetail(VBox cell) {
    	// TODO: DEBUG
    	System.out.println("135: Collapse Album Detail");
    	
    	// Removes the songBox from the flow pane.
    	grid.getChildren().remove(songBox);
    	isAlbumDetailCollapsed = true;
    	songBox.setVisible(false);
    }
    
    private void populateSongTable(VBox cell, Album selectedAlbum) {
    	// TODO: DEBUG
    	System.out.println("Populate Song Table");
    	
    	System.out.println("Album Title: " + selectedAlbum.getTitle());
    	
    	// Retrieves albums songs and stores them as an observable list.
    	ObservableList<Song> albumSongs = FXCollections.observableArrayList(selectedAlbum.getSongs());
    	
    	System.out.println("Album First Song (OL): " + albumSongs.get(0).getTitle());
    	
        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        // Sets each column item.
        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));
        
        // Adds songs to table.
        songTable.setItems(albumSongs);
    	
    }
}

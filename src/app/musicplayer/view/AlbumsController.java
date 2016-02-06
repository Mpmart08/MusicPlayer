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
import app.musicplayer.util.Scrollable;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AlbumsController implements Initializable, Scrollable {
	
    @FXML private ScrollPane gridBox;
	@FXML private FlowPane grid;
    @FXML private VBox songBox;
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    
    private boolean isAlbumDetailCollapsed = true;
    
    // Initializes values used for animations.
    private double expandedHeightReload = 50;
    private double collapsedHeightReload = 0;
    private double expandedHeight = 400;
    private double collapsedHeight = 0;
    
    // Initializes the index for the currently selected cell.
    private int currentCell;
    
    // Initializes the value of the x-coordinate for the currently selected cell.
    private double currentCellYCoordinate;
    
    // ANIMIATIONS
    
    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> collapseAlbumDetail());
        }
        protected void interpolate(double frac) {
        	double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1.0 - frac);
            songBox.setPrefHeight(curHeight);
            songBox.setOpacity(1.0 - frac);
        }
    };

    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
        }
        protected void interpolate(double frac) {
        	double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            songBox.setPrefHeight(curHeight);
            songBox.setOpacity(frac);
        }
    };
    
    private Animation songTableReloadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeightReload + (expandedHeightReload - collapsedHeightReload) * (frac);
            songTable.setTranslateY(expandedHeightReload - curHeight);
            songTable.setOpacity(frac);
        }
    };
    
    @Override
    public void scroll(char letter) {
    	
	    if (!isAlbumDetailCollapsed) {
	    	
	    	PseudoClass selected = PseudoClass.getPseudoClass("selected");
			
	    	for (Node child : grid.getChildren()) {
				child.pseudoClassStateChanged(selected, false);
			}
			
			// Plays the collapse animation to remove the song table.
			collapseAnimation.play();
	    }
		
    	int index = 0;
    	double cellHeight = 0;
    	ObservableList<Node> children = grid.getChildren();
    	
    	for (int i = 0; i < children.size(); i++) {
    		
    		VBox cell = (VBox) children.get(i);
    		cellHeight = cell.getHeight();
    		if (cell.getChildren().size() > 1) {
    			Label label = (Label) cell.getChildren().get(1);
        		char firstLetter = removeArticle(label.getText()).charAt(0);
        		if (firstLetter < letter) {
        			index++;
        		}	
    		}
    	}
    	
    	double row = (index / 5) * cellHeight;
    	double finalVvalue = row / (grid.getHeight() - gridBox.getHeight());
    	double startVvalue = gridBox.getVvalue();
    	
    	Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }
            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                gridBox.setVvalue(vValue);
            }
        };
        
        scrollAnimation.play();
    }
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		ObservableList<Album> albums = Library.getAlbums();
		Collections.sort(albums);

        int limit = (albums.size() < 25) ? albums.size() : 25;

		for (int i = 0; i < limit; i++) {

            Album album = albums.get(i);
            grid.getChildren().add(createCell(album, i));
		}

        int rows = (albums.size() % 5 == 0) ? albums.size() / 5 : albums.size() / 5 + 1;
        
        // Sets the height and width of the grid to fill the screen.
        grid.prefHeightProperty().bind(gridBox.widthProperty().divide(5).add(16).multiply(rows));
        grid.prefWidthProperty().bind(gridBox.widthProperty());
        
		// Sets the song table to be invisible when the view is initialized.
        songBox.setVisible(false);

        new Thread(() -> {

        	try {
        		Thread.sleep(1000);
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        	
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
        
        // Sets the playing properties for the songs in the song table.
        songTable.setRowFactory(x -> {
            TableRow<Song> row = new TableRow<Song>();

            PseudoClass playing = PseudoClass.getPseudoClass("playing");

            ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) -> {
                row.pseudoClassStateChanged(playing, newValue.booleanValue());
            };

            row.itemProperty().addListener((obs, previousSong, currentSong) -> {
            	if (previousSong != null) {
            		previousSong.playingProperty().removeListener(changeListener);
            	}
            	if (currentSong != null) {
                    currentSong.playingProperty().addListener(changeListener);
                    row.pseudoClassStateChanged(playing, currentSong.getPlaying());
                } else {
                    row.pseudoClassStateChanged(playing, false);
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Song song = row.getItem();
                    Album album = Library.getAlbum(song.getAlbum());
                    ArrayList<Song> songs = album.getSongs();
                    if (MusicPlayer.isShuffleActive()) {
                    	Collections.shuffle(songs);
                    	songs.remove(song);
                    	songs.add(0, song);
                    }
                    MusicPlayer.setNowPlayingList(songs);
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });

            return row ;
        });
	}

    private VBox createCell(Album album, int index) {

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
        title.prefWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));

        image.fitWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        image.fitHeightProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        image.setPreserveRatio(true);
        image.setSmooth(true);

        imageBox.prefWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        imageBox.prefHeightProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(image);

        cell.getChildren().addAll(imageBox, title);
        cell.setPadding(new Insets(10, 10, 10, 10));
        cell.getStyleClass().add("album-cell");
        cell.setAlignment(Pos.CENTER);
        cell.setOnMouseClicked(event -> {
        	
        	PseudoClass selected = PseudoClass.getPseudoClass("selected");
        	
        	// If the album detail is collapsed, expand it and populate song table.
        	if (isAlbumDetailCollapsed) {
        		
        		cell.pseudoClassStateChanged(selected, true);
        		
            	// Updates the index of the currently selected cell.
            	currentCell = index;
            	
        		// Shows song table, plays load animation and populates song table with album songs.
        		expandAlbumDetail(cell, index);
        		expandAnimation.play();
        		
        		populateSongTable(cell, album);
        		
        		// Else if album detail is expanded and opened album is reselected.
        	} else if (!isAlbumDetailCollapsed && index == currentCell) {
        		
        		cell.pseudoClassStateChanged(selected, false);
        		
        		// Plays the collapse animation to remove the song table.
        		collapseAnimation.play();
        		
        		// Else if album detail is expanded and a different album is selected on the same row.
        	} else if (!isAlbumDetailCollapsed && !(index == currentCell)
        			&& currentCellYCoordinate == cell.getBoundsInParent().getMaxY()) {
        		
        		for (Node child : grid.getChildren()) {
        			child.pseudoClassStateChanged(selected, false);
        		}
        		cell.pseudoClassStateChanged(selected, true);
        		
            	// Updates the index of the currently selected cell.
            	currentCell = index;
            	
            	// Plays load animation and populates song table with songs of newly selected album.
            	songTableReloadAnimation.play();
        		populateSongTable(cell, album);
        		
        		// Else if album detail is expanded and a different album is selected on a different row.
        	} else if (!isAlbumDetailCollapsed && !(index == currentCell)
        			&& !(currentCellYCoordinate == cell.getBoundsInParent().getMaxY())) {
        		
        		for (Node child : grid.getChildren()) {
        			child.pseudoClassStateChanged(selected, false);
        		}
        		cell.pseudoClassStateChanged(selected, true);
        		
            	// Updates the index of the currently selected cell.
            	currentCell = index;
            	
            	// Collapses the song table and then expands it in the appropriate row with songs on new album.
            	collapseAlbumDetail();
        		expandAlbumDetail(cell, index);
            	songTableReloadAnimation.play();
        		populateSongTable(cell, album);
        	} else {
        		
        		for (Node child : grid.getChildren()) {
        			child.pseudoClassStateChanged(selected, false);
        		}
        		
        		// Plays the collapse animation to remove the song table.
        		collapseAnimation.play();
        	}
        	// Sets the cells max x value as the current cell x coordinate.
        	currentCellYCoordinate = cell.getBoundsInParent().getMaxY();
        });
        return cell;
    }
    
    private void expandAlbumDetail(VBox cell, int index) {
    	isAlbumDetailCollapsed = false;
    	songBox.setVisible(true);
    }
    
    private void collapseAlbumDetail() {
    	isAlbumDetailCollapsed = true;
    	songBox.setVisible(false);
    }
    
    private void populateSongTable(VBox cell, Album selectedAlbum) {    	
    	// Retrieves albums songs and stores them as an observable list.
    	ObservableList<Song> albumSongs = FXCollections.observableArrayList(selectedAlbum.getSongs());
    	
        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        // Sets each column item.
        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));
        
        // Adds songs to table.
        songTable.setItems(albumSongs);
    }
    
    private String removeArticle(String title) {

        String arr[] = title.split(" ", 2);

        if (arr.length < 2) {
            return title;
        } else {

            String firstWord = arr[0];
            String theRest = arr[1];

            switch (firstWord) {
                case "A":
                case "An":
                case "The":
                    return theRest;
                default:
                    return title;
            }
        }
    }
}

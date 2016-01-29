package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.Scrollable;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

public class SongsController implements Initializable, Scrollable {

    @FXML private ScrollPane scrollPane;
	@FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    
    private String currentSort = "title";
    
    // TODO: GET CURRENT SONG SORT (BY SONG TITLE, ARTIST, ALBUM)
    
    @Override
    public void scroll(char letter) {
    	// Gets the current sorting of the song table.
    	// getSortPolicy getSortOrder getOnSort
    	
    	ObservableList<Song> songTableItems = tableView.getItems();
    	
    	int selectedCell = 0;
    	
    	if (currentSort.equals("title")) {
        	for (int i = 0; i < songTableItems.size(); i++) {
        		// Removes article from artist title and compares it to selected letter.
        		String songTitle = songTableItems.get(i).getTitle();
        		
        		try {
					char firstLetter = songTitle.charAt(0);
					if (firstLetter < letter) {
						selectedCell++;
					}
				} catch (NullPointerException npe) {
					System.out.println("Null Song Title");
//					npe.printStackTrace();
				}
        		
        	}
    	} else if (currentSort.equals("album")) {
        	for (int i = 0; i < songTableItems.size(); i++) {
        		// Removes article from artist title and compares it to selected letter.
        		String songAlbum = songTableItems.get(i).getAlbum();
        		char firstLetter = songAlbum.charAt(0);
        		
        		if (firstLetter < letter) {
            		selectedCell++;
        		}
        	}
    	} else {
        	for (int i = 0; i < songTableItems.size(); i++) {
        		// Removes article from artist title and compares it to selected letter.
        		String songArtist = songTableItems.get(i).getArtist();
        		char firstLetter = songArtist.charAt(0);
        		
        		if (firstLetter < letter) {
            		selectedCell++;
        		}
        	}
    	}
    	
    	double startVvalue = scrollPane.getVvalue();
    	
    	double finalVvalue = (double) (selectedCell * 50) / (tableView.getHeight() - scrollPane.getHeight()) + 
    			50 / (tableView.getHeight() - scrollPane.getHeight());
    	
    	Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }
            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                scrollPane.setVvalue(vValue);
            }
        };
        scrollAnimation.play();
    	

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        titleColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        albumColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));

        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        artistColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        albumColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));
        
        lengthColumn.setSortable(false);
        playsColumn.setSortable(false);
        
        // TODO:
        // Updates the currentSort string which is helps determine which column is being used to sort the table.
        
        // Retrieves the list of songs in the library, sorts them, and adds them to the table.
        ObservableList<Song> songs = Library.getSongs();
        
        if (songs == null) {
        	System.out.println("Songs .equals null");
        } else {
        	System.out.println("Songs IS NOT null");
        }
        
        System.out.println("Before sort");
        Collections.sort(songs, (Song x, Song y) -> {
        	// Song Title
        	if (x.getTitle() == null && y.getTitle() == null) {
        		// Both are equal.
        		return 0;
        	} else if (x.getTitle() == null && y.getTitle() != null) {
        		// Null is after other strings.
        		return 1;
        	} else if (y.getTitle() == null) {
        		// All other strings are before null.
        		return -1;
        	} else if (x.getTitle() != null && y.getTitle() != null) {
        		return x.getTitle().compareTo(y.getTitle());
        	}
        	
        	// Song Artist
        	if (x.getArtist() == null && y.getArtist() == null) {
        		// Both are equal.
        		return 0;
        	} else if (x.getArtist() == null && y.getArtist() != null) {
        		// Null is after other strings.
        		return 1;
        	} else if (y.getArtist() == null) {
        		// All other strings are before null.
        		return -1;
        	} else if (x.getArtist() != null && y.getArtist() != null) {
        		return x.getArtist().compareTo(y.getArtist());
        	}
        	
        	// Song Album
        	if (x.getAlbum() == null && y.getAlbum() == null) {
        		// Both are equal.
        		return 0;
        	} else if (x.getAlbum() == null && y.getAlbum() != null) {
        		// Null is after other strings.
        		return 1;
        	} else if (y.getAlbum() == null) {
        		// All other strings are before null.
        		return -1;
        	} else if (x.getAlbum() != null && y.getAlbum() != null) {
        		return x.getAlbum().compareTo(y.getAlbum());
        	} else {
        		// Default case.
        		return x.getTitle().compareTo(y.getTitle());
        	}
        });
        System.out.println("After sort");
        
        tableView.setItems(songs);
        
        // Sets the table view height to the height required to fit the scroll pane with all the artists.
        // This is important so that the scrolling is done in the scroll pane which is needed for the scroll animation.
        tableView.setPrefHeight(50*songs.size());
        tableView.setMinHeight(tableView.getPrefHeight());

        tableView.setRowFactory(x -> {
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
                    ArrayList<Song> songList = new ArrayList<Song>(Library.getSongs());
                    if (MusicPlayer.isShuffleActive()) {
                    	Collections.shuffle(songs);
                    	songs.remove(song);
                    	songs.add(0, song);
                    }
                    MusicPlayer.setNowPlayingList(songList);
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });

            return row ;
        });
        
        artistColumn.setComparator((first, second) -> {
        	return Library.getArtist(first).compareTo(Library.getArtist(second));
        });
        
        albumColumn.setComparator((first, second) -> {
        	return Library.getAlbum(first).compareTo(Library.getAlbum(second));
        });
    }
}

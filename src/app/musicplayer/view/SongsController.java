package app.musicplayer.view;

import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.Scrollable;
import app.musicplayer.util.SongTitleComparator;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

public class SongsController implements Initializable, Scrollable {

	@FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    
    // Initializes table view scroll bar.
    private ScrollBar scrollBar;
    
    // Keeps track of which column is being used to sort table view and in what order (ascending or descending)
    private String currentSortColumn = "titleColumn";
    private String currentSortOrder = null;
    
    @Override
    public void scroll(char letter) {
    	
    	if (tableView.getSortOrder().size() > 0) {
    		currentSortColumn = tableView.getSortOrder().get(0).getId();
    		
    		System.out.println(tableView.getSortOrder().get(0).getSortType().toString().toLowerCase());
    		currentSortOrder = tableView.getSortOrder().get(0).getSortType().toString().toLowerCase();
    		System.out.println("Current Sort Order: " + currentSortOrder);
    		
    	}
    	
    	// Retrieves songs from table.
    	ObservableList<Song> songTableItems = tableView.getItems();
    	// Initializes counter for cells. Used to determine what cell to scroll to.
    	int selectedCell = 0;
    	int selectedLetterCount = 0;
    	
    	// Retrieves the table view scroll bar.
    	if (scrollBar == null) {
    		scrollBar = (ScrollBar) tableView.lookup(".scroll-bar");
    	}
    	
    	if (currentSortColumn.equals("titleColumn")) {
        	for (int i = 0; i < songTableItems.size(); i++) {
        		// Gets song title and compares first letter to selected letter.
        		String songTitle = songTableItems.get(i).getTitle();
        		try {
					char firstLetter = songTitle.charAt(0);
					if (firstLetter < letter) {
						selectedCell++;
					} else if (firstLetter == letter) {
						selectedLetterCount ++;
					}
				} catch (NullPointerException npe) {
//					System.out.println("Null Song Title");
				}
        		
        	}
    	} else if (currentSortColumn.equals("artistColumn")) {
        	for (int i = 0; i < songTableItems.size(); i++) {
        		// Removes article from song artist and compares it to selected letter.
        		String songArtist = songTableItems.get(i).getArtist();
        		try {
					char firstLetter = removeArticle(songArtist).charAt(0);
					if (firstLetter < letter) {
						selectedCell++;
					} else if (firstLetter == letter) {
						selectedLetterCount ++;
					}
				} catch (NullPointerException npe) {
					System.out.println("Null Song Artist");
				}
        	}
    	} else if (currentSortColumn.equals("albumColumn")) {
        	for (int i = 0; i < songTableItems.size(); i++) {
        		// Removes article from song album and compares it to selected letter.
        		String songAlbum = songTableItems.get(i).getAlbum();
        		try {
					char firstLetter = removeArticle(songAlbum).charAt(0);
					if (firstLetter < letter) {
						selectedCell++;
					} else if (firstLetter == letter) {
						selectedLetterCount ++;
					}
				} catch (NullPointerException npe) {
					System.out.println("Null Song Album");
				}
        	}
    	}
    	
    	double startVvalue = scrollBar.getValue();
    	double finalVvalue;
    	
    	if ("descending".equals(currentSortOrder)) {
    		finalVvalue = 1 - ((double) ((selectedCell + selectedLetterCount) * 50 - scrollBar.getHeight()) /
    				(songTableItems.size() * 50 - scrollBar.getHeight()));
    	} else {
    		finalVvalue = (double) (selectedCell * 50) / (songTableItems.size() * 50 - scrollBar.getHeight());
    	}
    	
    	// TODO: DEBUG
    	System.out.println("Start V Value: " + startVvalue);
    	System.out.println("Final V Value: " + finalVvalue);
    	
    	Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }
            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                scrollBar.setValue(vValue);
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
        
        // Retrieves the list of songs in the library, sorts them, and adds them to the table.
        ObservableList<Song> songs = Library.getSongs();

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
        		String xArtist = x.getArtist();
        		String yArtist = y.getArtist();
        		return removeArticle(xArtist).compareTo(removeArticle(yArtist));
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
        		String xAlbum = x.getAlbum();
        		String yAlbum = y.getAlbum();
        		return removeArticle(xAlbum).compareTo(removeArticle(yAlbum));
        	} else {
        		// Default case.
        		return x.getTitle().compareTo(y.getTitle());
        	}
        });
        
        tableView.setItems(songs);

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
                    ObservableList<Song> songList = Library.getSongs();
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
        
        titleColumn.setComparator(new SongTitleComparator<String>());
        
        artistColumn.setComparator((first, second) -> {
        	return Library.getArtist(first).compareTo(Library.getArtist(second));
        });
        
        albumColumn.setComparator((first, second) -> {
        	return Library.getAlbum(first).compareTo(Library.getAlbum(second));
        });
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

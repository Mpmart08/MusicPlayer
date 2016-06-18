package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.ControlPanelTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.SubView;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Duration;

public class SongsController implements Initializable, SubView {

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
    
    private Song selectedSong;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    	
    	titleColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        albumColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));

        playingColumn.setCellFactory(x -> new PlayingTableCell<>());
        titleColumn.setCellFactory(x -> new ControlPanelTableCell<>());
        artistColumn.setCellFactory(x -> new ClippedTableCell<>());
        albumColumn.setCellFactory(x -> new ClippedTableCell<>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<>("playCount"));
        
        lengthColumn.setSortable(false);
        playsColumn.setSortable(false);
        
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        	tableView.requestFocus();
        	event.consume();
        });
        
        // Retrieves the list of songs in the library, sorts them, and adds them to the table.
        ObservableList<Song> songs = Library.getSongs();

        Collections.sort(songs, (x, y) -> compareSongs(x, y));
        
        tableView.setItems(songs);

        tableView.setRowFactory(x -> {
            TableRow<Song> row = new TableRow<>();

            PseudoClass playing = PseudoClass.getPseudoClass("playing");

            ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) ->
                    row.pseudoClassStateChanged(playing, newValue);

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
            	TableViewSelectionModel<Song> sm = tableView.getSelectionModel();
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    play();
                } else if (event.isShiftDown()) {
                	ArrayList<Integer> indices = new ArrayList<>(sm.getSelectedIndices());
                	if (indices.size() < 1) {
                		if (indices.contains(row.getIndex())) {
                    		sm.clearSelection(row.getIndex());
                    	} else {
                    		sm.select(row.getItem());
                    	}
                	} else {
                		sm.clearSelection();
	                	indices.sort((first, second) -> first.compareTo(second));
	                	int max = indices.get(indices.size() - 1);
	                	int min = indices.get(0);
	                	if (min < row.getIndex()) {
	                		for (int i = min; i <= row.getIndex(); i++) {
	                			sm.select(i);
	                		}
	                	} else {
	                		for (int i = row.getIndex(); i <= max; i++) {
	                			sm.select(i);
	                		}
	                	}
                	}
                	
                } else if (event.isControlDown()) {
                	if (sm.getSelectedIndices().contains(row.getIndex())) {
                		sm.clearSelection(row.getIndex());
                	} else {
                		sm.select(row.getItem());
                	}
                } else {
                	if (sm.getSelectedIndices().size() > 1) {
                		sm.clearSelection();
                    	sm.select(row.getItem());
                	} else if (sm.getSelectedIndices().contains(row.getIndex())) {
                		sm.clearSelection();
                	} else {
                		sm.clearSelection();
                    	sm.select(row.getItem());
                	}
                }
            });
            
            row.setOnDragDetected(event -> {
            	Dragboard db = row.startDragAndDrop(TransferMode.ANY);
            	ClipboardContent content = new ClipboardContent();
            	if (tableView.getSelectionModel().getSelectedIndices().size() > 1) {
            		content.putString("List");
                    db.setContent(content);
                	MusicPlayer.setDraggedItem(tableView.getSelectionModel().getSelectedItems());
            	} else {
            		content.putString("Song");
                    db.setContent(content);
                	MusicPlayer.setDraggedItem(row.getItem());
            	}
            	ImageView image = new ImageView(row.snapshot(null, null));
            	Rectangle2D rectangle = new Rectangle2D(0, 0, 250, 50);
            	image.setViewport(rectangle);
            	db.setDragView(image.snapshot(null, null), 125, 25);
                event.consume();
            });

            return row ;
        });
        
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        	if (oldSelection != null) {
        		oldSelection.setSelected(false);
        	}
        	if (newSelection != null && tableView.getSelectionModel().getSelectedIndices().size() == 1) {
        		newSelection.setSelected(true);
        		selectedSong = newSelection;
        	}
        });
        
        // Plays selected song when enter key is pressed.
        tableView.setOnKeyPressed(event -> {
        	if (event.getCode().equals(KeyCode.ENTER)) {
        		play();
        	}
        });
        
        titleColumn.setComparator((x, y) -> {
        	
        	if (x == null && y == null) {
				return 0;
			} else if (x == null) {
				return 1;
			} else if (y == null) {
				return -1;
			}
        	
        	Song first = Library.getSong(x);
        	Song second = Library.getSong(y);
        	
        	return compareSongs(first, second);
        });
        
        artistColumn.setComparator((first, second) -> Library.getArtist(first).compareTo(Library.getArtist(second)));
        
        albumColumn.setComparator((first, second) -> Library.getAlbum(first).compareTo(Library.getAlbum(second)));
    }
    
    private int compareSongs(Song x, Song y) {
    	if (x == null && y == null) {
    		return 0;
    	} else if (x == null) {
    		return 1;
    	} else if (y == null) {
    		return -1;
    	}
    	if (x.getTitle() == null && y.getTitle() == null) {
    		// Both are equal.
    		return 0;
    	} else if (x.getTitle() == null) {
    		// Null is after other strings.
    		return 1;
		} else if (y.getTitle() == null) {
			// All other strings are before null.
			return -1;
		} else  /*(x.getTitle() != null && y.getTitle() != null)*/ {
			return x.getTitle().compareTo(y.getTitle());
		}
	}
    
    @Override
    public void play() {
    	
    	Song song = selectedSong;
        ObservableList<Song> songList = tableView.getItems();
        if (MusicPlayer.isShuffleActive()) {
        	Collections.shuffle(songList);
        	songList.remove(song);
        	songList.add(0, song);
        }
        MusicPlayer.setNowPlayingList(songList);
        MusicPlayer.setNowPlaying(song);
        MusicPlayer.play();
    }
    
    @Override
    public void scroll(char letter) {
    	
    	if (tableView.getSortOrder().size() > 0) {
    		currentSortColumn = tableView.getSortOrder().get(0).getId();
    		currentSortOrder = tableView.getSortOrder().get(0).getSortType().toString().toLowerCase();
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

        switch (currentSortColumn) {
            case "titleColumn":
                for (Song song : songTableItems) {
                    // Gets song title and compares first letter to selected letter.
                    String songTitle = song.getTitle();
                    try {
                        char firstLetter = songTitle.charAt(0);
                        if (firstLetter < letter) {
                            selectedCell++;
                        } else if (firstLetter == letter) {
                            selectedLetterCount++;
                        }
                    } catch (NullPointerException npe) {
                        System.out.println("Null Song Title");
                    }

                }
                break;
            case "artistColumn":
                for (Song song : songTableItems) {
                    // Removes article from song artist and compares it to selected letter.
                    String songArtist = song.getArtist();
                    try {
                        char firstLetter = removeArticle(songArtist).charAt(0);
                        if (firstLetter < letter) {
                            selectedCell++;
                        } else if (firstLetter == letter) {
                            selectedLetterCount++;
                        }
                    } catch (NullPointerException npe) {
                        System.out.println("Null Song Artist");
                    }
                }
                break;
            case "albumColumn":
                for (Song song : songTableItems) {
                    // Removes article from song album and compares it to selected letter.
                    String songAlbum = song.getAlbum();
                    try {
                        char firstLetter = removeArticle(songAlbum).charAt(0);
                        if (firstLetter < letter) {
                            selectedCell++;
                        } else if (firstLetter == letter) {
                            selectedLetterCount++;
                        }
                    } catch (NullPointerException npe) {
                        System.out.println("Null Song Album");
                    }
                }
                break;
        }
    	
    	double startVvalue = scrollBar.getValue();
    	double finalVvalue;
    	
    	if ("descending".equals(currentSortOrder)) {
    		finalVvalue = 1 - (((selectedCell + selectedLetterCount) * 50 - scrollBar.getHeight()) /
    				(songTableItems.size() * 50 - scrollBar.getHeight()));
    	} else {
    		finalVvalue = (double) (selectedCell * 50) / (songTableItems.size() * 50 - scrollBar.getHeight());
    	}
    	
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
    
    public Song getSelectedSong() {
    	return selectedSong;
    }
}

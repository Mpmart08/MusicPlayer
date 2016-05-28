package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Library;
import app.musicplayer.model.MostPlayedPlaylist;
import app.musicplayer.model.Playlist;
import app.musicplayer.model.RecentlyPlayedPlaylist;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.ControlPanelTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.SubView;
import app.musicplayer.util.XMLEditor;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class PlaylistsController implements Initializable, SubView {

    @FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    
    @FXML private Label playlistTitleLabel;
    @FXML private HBox controlBox;
    @FXML private Pane deleteButton;

    private Playlist selectedPlaylist;
    private Song selectedSong;
    
    // Used to store the individual playlist boxes from the playlistBox. 
    private HBox cell;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    	titleColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        albumColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.26));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.11));

        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ControlPanelTableCell<Song, String>());
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
        
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        	event.consume();
        });

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
            	TableViewSelectionModel<Song> sm = tableView.getSelectionModel();
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    play();
                } else if (event.isShiftDown()) {
                	ArrayList<Integer> indices = new ArrayList<Integer>(sm.getSelectedIndices());
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

            return row;
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
    }

    public void selectPlaylist(Playlist playlist) {
    	// Displays the delete button only if the user has not selected one of the default playlists.
    	if (playlist instanceof MostPlayedPlaylist || playlist instanceof RecentlyPlayedPlaylist) {
    		deleteButton.setVisible(false);
    	}
    	
    	// Sets the text on the play list title label.
    	playlistTitleLabel.setText(playlist.getTitle());
    	
    	// Updates the currently selected play list.
    	selectedPlaylist = playlist;
    	Library.setSelectedPlaylist(playlist);
    	
    	// Retrieves the songs in the selected play list.
        ObservableList<Song> songs = playlist.getSongs();
        
        // Clears the song table.
        tableView.getSelectionModel().clearSelection();
        
        if (songs.isEmpty()) {
        	songs.add(null);
        }
        
        // Populates the song table with the playlist's songs.
        tableView.setItems(songs);
    }
    
    @Override
    public void scroll(char letter) {};
    
    @Override
    public void play() {
    	Song song = selectedSong;
        ObservableList<Song> songs = selectedPlaylist.getSongs();
        if (MusicPlayer.isShuffleActive()) {
        	Collections.shuffle(songs);
        	songs.remove(song);
        	songs.add(0, song);
        }
        MusicPlayer.setNowPlayingList(songs);
        MusicPlayer.setNowPlaying(song);
        MusicPlayer.play();
    }
    
    @Override
    public Song getSelectedSong() {
    	return selectedSong;
    }
    
    public Playlist getSelectedPlaylist() {
    	return selectedPlaylist;
    }
    
    public void deleteSelectedRow() {    	
		// Retrieves the table view items and the selected item.
    	ObservableList<Song> allSongs, selectedSong;
    	allSongs = tableView.getItems();
    	selectedSong = tableView.getSelectionModel().getSelectedItems();
    	
    	// Removes the selected item from the table view.
    	selectedSong.forEach(allSongs::remove);
    }
    
    @FXML
    private void playPlaylist() {
    	ObservableList<Song> songs = selectedPlaylist.getSongs();
    	MusicPlayer.setNowPlayingList(songs);
    	MusicPlayer.setNowPlaying(songs.get(0));
    	MusicPlayer.play();
    }
    
    @FXML
    private void deletePlaylist() {
    	if (!deletePlaylistAnimation.getStatus().equals(Status.RUNNING)) {        	
        	// Gets the title of the selected playlist to compare it against the labels of the playlist boxes.
        	String selectedPlaylistTitle = Library.getSelectedPlaylist().getTitle();
        	
        	// Gets the playlist box children to loop through each to find the correct child to remove.
        	ObservableList<Node> playlistBoxChildren = MusicPlayer.getMainController().getPlaylistBox().getChildren();
        	
        	// Initialize i at 1 to ignore the new playlist cell. 
        	for (int i = 1; i <= playlistBoxChildren.size(); i++) {
        		// Gets each cell in the playlist box and retrieves the cell's label.
        		cell = (HBox) playlistBoxChildren.get(i);
        		Label cellLabel = (Label) cell.getChildren().get(1);
        		
        		// Ends the process if the cell's label matches the selected playlist's title.
        		if (cellLabel.getText().equals(selectedPlaylistTitle)) {
        			break;
        		}
        	}
        	
        	deletePlaylistAnimation.play();
        	
        	// Deletes the play list from the xml file.
        	XMLEditor.deletePlaylistFromXML(selectedPlaylist.getId());
    		
        	// Loads the artists view.
        	MusicPlayer.getMainController().loadView("artists");
        	
        	// Removes the selected playlist from the library so that it is not reloaded.
        	Library.removePlaylist(selectedPlaylist);
        	
        	// Resets the selected playlist to avoid storing the deleted playlist's data.
        	selectedPlaylist = null;
    	}
    }
    
    public Animation deletePlaylistAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(500));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {        	    		
    		if (frac < 0.5) {
    			cell.setPrefHeight(cell.getHeight() - frac * 10);
    		} else {
    			cell.setPrefHeight(0);
    			cell.setOpacity(0);
    		}
        }
    };
}
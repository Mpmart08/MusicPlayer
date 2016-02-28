package app.musicplayer.view;

import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Playlist;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.ControlPanelTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.SubView;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class PlaylistsController implements Initializable, SubView {

    @FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;

    private Playlist selectedPlaylist;
    private Song selectedSong;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        titleColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.35));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.35));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.15));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(50).multiply(0.15));

        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ControlPanelTableCell<Song, String>());
        artistColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));
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
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    play();
                } else {
                	tableView.getSelectionModel().select(row.getItem());
                }
            });
            
            row.setOnDragDetected(event -> {
            	Dragboard db = row.startDragAndDrop(TransferMode.ANY);
            	ClipboardContent content = new ClipboardContent();
                content.putString("Song");
                db.setContent(content);
            	MusicPlayer.setDraggedItem(row.getItem());
            	ImageView image = new ImageView(row.snapshot(null, null));
            	Rectangle2D rectangle = new Rectangle2D(0, 0, 250, 50);
            	image.setViewport(rectangle);
            	db.setDragView(image.snapshot(null, null));
                event.consume();
            });
            
            return row ;
        });
        
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        	if (oldSelection != null) {
        		oldSelection.setSelected(false);
        	}
        	if (newSelection != null) {
        		newSelection.setSelected(true);
        		selectedSong = newSelection;
        	}
        });
    }

    public void selectPlaylist(Playlist playlist) {

    	selectedPlaylist = playlist;
        ObservableList<Song> songs = playlist.getSongs();
        tableView.getSelectionModel().clearSelection();
        
        if (songs.isEmpty()) {
        	songs.add(null);
        }
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
}
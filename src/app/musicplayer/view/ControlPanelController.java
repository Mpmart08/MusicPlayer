package app.musicplayer.view;

import java.net.URL;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Library;
import app.musicplayer.model.Playlist;
import app.musicplayer.util.SubView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class ControlPanelController implements Initializable {
	
	@FXML private Pane playButton;
	@FXML private Pane playlistButton;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {}
	
	@FXML
	private void playSong(Event e) {
		SubView controller = MusicPlayer.getMainController().getSubViewController();
		controller.play();
		e.consume();
	}
	
	@FXML
	private void addToPlaylist(Event e) {
		// Gets the mouse event coordinates in the screen to display the context menu in this location.
		MouseEvent mouseEvent = (MouseEvent) e;
		double x = mouseEvent.getScreenX();
		double y = mouseEvent.getScreenY();
		
		// TODO: DEBUG
		System.out.println("CPC_43: get x = " + mouseEvent.getScreenX() + " get y = " + mouseEvent.getScreenY());
		
		ObservableList<Playlist> playlists = Library.getPlaylists();
		
		// Retrieves all the playlist titles to create menu items.
		ObservableList<String> playlistTitles = FXCollections.observableArrayList();
		for (Playlist playlist : playlists) {
			String title = playlist.getTitle();
			if (!(title.equals("Most Played") || title.equals("Recently Played"))) {
				playlistTitles.add(title);
			}
		}
		
		final ContextMenu contextMenu = new ContextMenu();
		
		// Creates a menu item for each playlist title and adds it to the context menu.
		for (String title : playlistTitles) {
			MenuItem item = new MenuItem(title);
			contextMenu.getItems().add(item);
		}
		
		contextMenu.show(playButton, x, y);
		
		e.consume();
	}
}

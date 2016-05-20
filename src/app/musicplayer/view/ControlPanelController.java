package app.musicplayer.view;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import app.musicplayer.MusicPlayer;
import app.musicplayer.util.Resources;
import app.musicplayer.util.SubView;
import app.musicplayer.util.XMLEditor;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

public class ControlPanelController implements Initializable {
	
	private boolean inPlaylists;
	private boolean inSelectedPlaylist;
	
	@FXML private Pane playButton;
	@FXML private Pane playlistButton;
	@FXML private Pane infoButton;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	
	@FXML
	private void playSong(Event e) {
		SubView controller = MusicPlayer.getMainController().getSubViewController();
		controller.play();
		e.consume();
	}
	
	@FXML
	private void addToPlaylist(Event e) {
		e.consume();
	}
	
	@FXML
	private void showInfo(Event e) {		
		// Gets the play lists controller sub view, which keeps track of the currently selected song.
		// A PlayListsController object will always be returned since this button will only be visible
		// when the user selects a song while in a play list.
		PlaylistsController controller = (PlaylistsController) MusicPlayer.getMainController().getSubViewController();
		
		// Retrieves play list and song id to search for the song in the xml file.
		int selectedPlayListId = controller.getSelectedPlaylist().getId();
		int selectedSongId = controller.getSelectedSong().getId();
		
		// TODO: DEBUG
		System.out.println("CPC_58: selected playlist = " + selectedPlayListId + " selected song = " + selectedSongId);
		
		// Calls methods to delete selected song from play list.
		XMLEditor.deleteSongFromPlaylist(selectedPlayListId, selectedSongId);
		
		e.consume();
	}
}

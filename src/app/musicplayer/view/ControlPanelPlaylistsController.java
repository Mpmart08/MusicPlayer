package app.musicplayer.view;

import java.net.URL;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Library;
import app.musicplayer.util.SubView;
import app.musicplayer.util.XMLEditor;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

public class ControlPanelPlaylistsController implements Initializable {
	
	@FXML private Pane playButton;
	@FXML private Pane deleteButton;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {}
	
	@FXML
	private void playSong(Event e) {
		SubView controller = MusicPlayer.getMainController().getSubViewController();
		controller.play();
		e.consume();
	}
	
	@FXML
	private void deleteSong(Event e) {		
		// Gets the play lists controller sub view, which keeps track of the currently selected song.
		// A PlayListsController object will always be returned since this button will only be visible
		// when the user selects a song while in a play list.
		PlaylistsController controller = (PlaylistsController) MusicPlayer.getMainController().getSubViewController();
		
		// Retrieves play list and song id to search for the song in the xml file.
		int selectedPlayListId = controller.getSelectedPlaylist().getId();
		int selectedSongId = controller.getSelectedSong().getId();
		
		// Calls methods to delete selected song from play list in XML file.
		XMLEditor.deleteSongFromPlaylist(selectedPlayListId, selectedSongId);

        // Removes the selected song from the playlist's song list in Library.
        Library.getPlaylist(selectedPlayListId).removeSong(selectedSongId);
		
		// Deletes the selected row from the table view.
		controller.deleteSelectedRow();
		
		e.consume();
	}
}

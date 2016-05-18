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
		// Gets the playlists controller sub view, which keeps track of the currently selected song.
		// A PlayListsController object will always be returned since this button will only be visible
		// when the user selects a song while in a playlist.
		PlaylistsController controller = (PlaylistsController) MusicPlayer.getMainController().getSubViewController();
		
		// Retrieves play list and song id to search for the song in the xml file.
		int selectedPlayListId = controller.getSelectedPlaylist().getId();
		int selectedSongId = controller.getSelectedSong().getId();
		
		// Searches current play list for the song to be deleted. 
		try {
			// Creates reader for xml file.
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true);
			FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
			XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = null;
			
			// Loops through xml file to find the currently selected song to delete it from the current play list.
			while(reader.hasNext()) {
			    reader.next();
			    if (reader.isWhiteSpace()) {
			        continue;
			    } else if (reader.isStartElement()) {
			    	element = reader.getName().getLocalPart();
			    	
			    	// Sets variable to true if the reader is currently looking at play lists data in the xml file.
			    	if (element.equals("playlists")) {
			    		inPlaylists = true;
			    		// TODO: DEBUG
				    	System.out.println("CPC_78: In Playlists");
			    	}
			    } else if (reader.isCharacters() && inPlaylists && element.equals("id")) {
			    	// Gets the id of the play list that the reader is currently looking at.
			    	int playListId = Integer.valueOf(reader.getText());
			    	
			    	// TODO: DEBUG
			    	System.out.println("CPC_85: PL ID = " + playListId + " | S PL ID = " + selectedPlayListId);
			    	
			    	// Checks if the id matches the id of the selected play list.
			    	if (playListId == selectedPlayListId) {
			    		inSelectedPlaylist = true;
			    		// TODO: DEBUG
			    		System.out.println("CPC_91: In selected playlist");
			    	}
			    } else if (reader.isCharacters() && inSelectedPlaylist && element.equals("songId")) {
			    	// Gets the id of the song that the reader is currently looking at.
			    	int songId = Integer.valueOf(reader.getText());
			    	
			    	// TODO: DEBUG
			    	System.out.println("CPC_98: S ID = " + songId + " | S S ID = " + selectedSongId);
			    	
			    	// Checks if the id matches the id of the selected play list.
			    	if (songId == selectedSongId) {
			    		// TODO: DELETE NODE
			    		
			    		// TODO: DEBUG
			    		System.out.println("CPC_100: Delete node");
			    		
				    	// TODO: ANIMATION FOR NODE REMOVAL
			    		
				    	break;
			    	}
			    }
			}
			// Closes xml reader.
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		e.consume();
	}
}

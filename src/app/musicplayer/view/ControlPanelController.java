package app.musicplayer.view;

import java.net.URL;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.util.SubView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

/**
 * 
 * @version 1.0
 *
 */
public class ControlPanelController implements Initializable {
	
	@FXML private Pane playButton;
	@FXML private Pane playlistButton;
	@FXML private Pane infoButton;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	
	@FXML
	private void playSong() {
		
		SubView controller = MusicPlayer.getMainController().getSubViewController();
		controller.play();
	}
	
	@FXML
	private void addToPlaylist() {
			
	}
	
	@FXML
	private void showInfo() {
		
	}
}

package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class AlbumsController implements Initializable {
	
	@FXML private ToggleButton sortByAlbum;
	@FXML private ToggleButton sortByArtist;
	
	// TODO: AQUI EN EN INITIALIZE?


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		ToggleGroup sortGroup = new ToggleGroup();
		sortByAlbum.setToggleGroup(sortGroup);
		sortByArtist.setToggleGroup(sortGroup);
	}
	
}

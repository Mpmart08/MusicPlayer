package app.musicplayer.view;

import java.net.URL;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.util.SliderSkin;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * 
 * @version 1.0
 *
 */
public class VolumePopupController implements Initializable {
	
	@FXML private Slider volumeSlider;
	@FXML private Region frontVolumeTrack;
	@FXML private Label volumeLabel;
	@FXML private Pane muteButton;
	@FXML private Pane mutedButton;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		try {
    		
        	SliderSkin skin = new SliderSkin(volumeSlider);
        	volumeSlider.setSkin(skin);
        	frontVolumeTrack.prefWidthProperty().bind(volumeSlider.widthProperty().subtract(30).multiply(volumeSlider.valueProperty().divide(volumeSlider.maxProperty())));
        	volumeSlider.valueProperty().addListener((x, y, z) -> {
        		volumeLabel.setText(Integer.toString(z.intValue()));
        	});
        	
    	} catch (Exception ex) {
    		
    		ex.printStackTrace();
    	}
	}
	
	public Slider getSlider() {
		return volumeSlider;
	}
	
	@FXML private void volumeClick() {
		MusicPlayer.getMainController().volumeClick();
	}
	
	@FXML private void muteClick() {
		
		PseudoClass muted = PseudoClass.getPseudoClass("muted");
		boolean isMuted = mutedButton.isVisible();
		muteButton.setVisible(isMuted);
		mutedButton.setVisible(!isMuted);
		volumeSlider.pseudoClassStateChanged(muted, !isMuted);
		frontVolumeTrack.pseudoClassStateChanged(muted, !isMuted);
		volumeLabel.pseudoClassStateChanged(muted, !isMuted);
		MusicPlayer.mute(isMuted);
	}
}

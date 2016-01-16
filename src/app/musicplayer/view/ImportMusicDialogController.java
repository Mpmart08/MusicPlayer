package app.musicplayer.view;

import app.musicplayer.model.Library;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ImportMusicDialogController {
	@FXML private Label label;
	@FXML private Button importMusicButton;
	
	private Stage dialogStage;
	private boolean musicImported = false;

	/**
	 * Initializes the controller class.
	 * This method is automatically called after the FXML file has been loaded.
	 */
	public void initialize() {}
	
	/**
	 * Sets the stage of this dialog.
	 * 
	 * @param dialogStage
	 */
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
	
	/**
	 * Returns true if the music library was imported successfully, false otherwise.
	 * 
	 * @return
	 */
	public boolean isMusicImported() {
		return musicImported;
	}
	
	@FXML
	private void handleImport() {
		try {
			DirectoryChooser directoryChooser = new DirectoryChooser();
		    // Show file explorer.
		    String musicDirectory = directoryChooser.showDialog(dialogStage).getPath();
		    // Creates library.xml file from user music library.
		    Library.importMusic(musicDirectory);
		    // Sets the music as imported successfully and closes the dialog.
		    musicImported = true;
		    dialogStage.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package app.musicplayer.view;

import app.musicplayer.model.Library;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ImportMusicDialogController {
	@FXML private Label label;
	@FXML private Button importMusicButton;
	@FXML private ProgressBar progressBar;
	
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

		    System.out.println("Before thread");		    
	    
	        Thread thread = new Thread() {
	        	public void run() {
				    // Creates library.xml file from user music library.
				    try {
						Library.importMusic(musicDirectory);
					} catch (Exception e) {
						e.printStackTrace();
					}
				    
				    Platform.runLater(new Runnable() {
				    	public void run() {
				        	System.out.println("Before change visibility");
				        	
				        	importMusicButton.setVisible(false);
						    progressBar.setVisible(true);
						    
						    System.out.println("After change visibility");
				    	}
				    });
	        	}
	        };
	        
	        thread.start();
	        thread.join();
	        
		    System.out.println("After thread");
		    
		    // Sets the music as imported successfully and closes the dialog.
		    musicImported = true;
		    dialogStage.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package app.musicplayer.view;

import java.nio.file.Path;

import app.musicplayer.model.Library;
import app.musicplayer.util.ImportMusicTask;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

/**
 * Dialog to update music library.
 *
 */
public class UpdateMusicDialogController {
	@FXML private Label label;
	@FXML private ProgressBar progressBar;
	
	private Stage dialogStage;
	private boolean musicUpdated = false;
	private String musicDirectory;

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
	
	public void setMusicDirectory(Path musicDirectory) {
		this.musicDirectory = musicDirectory.toString();
		
		// TODO: DEBUG
		System.out.println("UMDC46_: " + musicDirectory);
	}
	
	/**
	 * Returns true if the music library was imported successfully, false otherwise.
	 * 
	 * @return
	 */
	public boolean isMusicUpdated() {
		return musicUpdated;
	}
	
	public void handleUpdate() {
		
		// TODO: DEBUG
		System.out.println("UMDC61_In handle Update");
		
	    // Creates a task that is used to update the music library.
        ImportMusicTask<Boolean> task = new ImportMusicTask<Boolean>() {
        	@Override protected Boolean call() throws Exception {
		        // Creates library.xml file from user music library.
			    try {
					Library.importMusic(musicDirectory, this);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
        	}
        };
        
        // When the task (music importing) ends, the dialog is closed.
        task.setOnSucceeded((x) -> {
		    // Sets the music as imported successfully and closes the dialog.
		    musicUpdated = true;
		    dialogStage.close();
        });
        
        task.updateProgress(0, 1);
        
        // Retrieves the task progress and adds that to the progress bar.
        progressBar.progressProperty().bind(task.progressProperty());
        
        // Creates a new thread with the import music task and runs it.
        Thread thread = new Thread(task);
        thread.start();

	}
}

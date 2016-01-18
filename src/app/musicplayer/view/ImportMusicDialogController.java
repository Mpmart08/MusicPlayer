package app.musicplayer.view;

import app.musicplayer.model.Library;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Dialog to import music library.
 *
 */
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
		    
		    // Creates a task that is used to import the music library.
	        Task<Boolean> task = new Task<Boolean>() {
	        	@Override protected Boolean call() throws Exception {
			        // Creates library.xml file from user music library.
				    try {
						Library.importMusic(musicDirectory);
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
			    musicImported = true;
			    dialogStage.close();
	        });
	        
	        // Retrieves the task progress and adds that to the progress bar.
	        progressBar.progressProperty().bind(task.progressProperty());
	        
	        // Creates a new thread with the import music task and runs it.
	        Thread thread = new Thread(task);
	        thread.start();
        	
	        label.setText("Importing music library...");
	        // Makes the import music button invisible and the progress bar visible.
	        // This happens as soon as the music import task is started.
        	importMusicButton.setVisible(false);
		    progressBar.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

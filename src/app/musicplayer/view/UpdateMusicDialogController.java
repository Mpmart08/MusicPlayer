package app.musicplayer.view;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import app.musicplayer.model.Library;
import app.musicplayer.util.ImportMusicTask;
import app.musicplayer.util.Resources;
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
	
	// Stores song names in library xml file and music directory.
	ArrayList<String> xmlSongs = new ArrayList<String> ();
	ArrayList<String> musicDirSongs = new ArrayList<String> ();

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
	 * Sets the music directory path as a string for the dialog.
	 * 
	 * @param musicDirectory
	 */
	public void setMusicDirectory(Path musicDirectory) {
		this.musicDirectory = musicDirectory.toString();
		
		// TODO: DEBUG
		System.out.println("UMDC48_: " + musicDirectory);
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
		System.out.println("UMDC63_In handle Update");
		
	    // Creates a task that is used to update the music library.
        ImportMusicTask<Boolean> task = new ImportMusicTask<Boolean>() {
        	@Override protected Boolean call() throws Exception {
        		try {
            		// Finds the song titles in the library xml file and stores them in the librarySongs array list.
					xmlSongTitleFinder();
					// TODO: DEBUG
					System.out.println("UMDC88_XML Song Size: " + xmlSongs.size());
					
            		// Finds the song titles in the music directory and stores them in the librarySongs array list.
					musicDirSongTitleFinder(new File(musicDirectory));
					// TODO: DEBUG
					System.out.println("UMDC93_MUSIC DIR Song Size: " + musicDirSongs.size());
					
					// Loops through the xml songs and checks if they are in the music directory.
					// This checks if songs were deleted from the music directory.
					for (String song : xmlSongs) {
						// If they are not, the song was deleted from the music directory.
						if (!musicDirSongs.contains(song)) {
							// TODO: DEBUG
							System.out.println("UMDC108_Song deleted from music directory");
							
							// TODO: DELETE SONG FROM LIBRARY XML
							// Deletes song from library xml file.
						}
					}
					
					// Loops through the music dir songs and checks if they are in the library xml file.
					// This checks if songs were added to the music directory.
					for (String song : musicDirSongs) {
						// If they are not, the song was added to the music directory.
						if (!xmlSongs.contains(song)) {
							// TODO: DEBUG
							System.out.println("UMDC121_Song added to music directory");
							
							// TODO: ADD SONG TO LIBRARY XML
							// Adds the song to the library xml file.
							
						}
					}
					
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
        		
        		// TODO: MAYBE CREATE A LIBRARY.UPDATEMUSIC() ?
        		
//		        // Creates library.xml file from user music library.
//			    try {
//					Library.importMusic(musicDirectory, this);
//					return true;
//				} catch (Exception e) {
//					e.printStackTrace();
//					return false;
//				}
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
	
	private void xmlSongTitleFinder() {
		try {
			// Creates reader for xml file.
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true);
			FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
			XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = null;
			String songTitle = null;
			
			// Loops through xml file looking for song titles.
			// Stores the song title in the xmlSongs array list.
			while(reader.hasNext()) {
			    reader.next();
			    if (reader.isWhiteSpace()) {
			        continue;
			    } else if (reader.isStartElement()) {
			    	element = reader.getName().getLocalPart();
			    } else if (reader.isCharacters() && element.equals("title")) {
			    	songTitle = reader.getText();
			    	xmlSongs.add(songTitle);
			    }
			}
			// Closes xml reader.
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void musicDirSongTitleFinder(File musicDirectoryFile) {
    	// Lists all the files in the music directory and stores them in an array.
        File[] files = musicDirectoryFile.listFiles();

        // Loops through the files.
        for (File file : files) {
            if (file.isFile()) {
            	try {
            		// Gets the song title and adds it to the musicDirSongs array list.
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();
                    musicDirSongs.add(tag.getFirst(FieldKey.TITLE));
				} catch (Exception e) {
					e.printStackTrace();
				}
            	
            } else if (file.isDirectory()) {
            	musicDirSongTitleFinder(file);
            }
        }
	}
}

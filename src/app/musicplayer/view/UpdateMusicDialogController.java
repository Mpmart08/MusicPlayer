package app.musicplayer.view;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import app.musicplayer.model.Library;
import app.musicplayer.util.ImportMusicTask;
import app.musicplayer.util.Resources;
import app.musicplayer.util.XMLEditor;
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
	
	// Initializes booleans used to determine if the library.xml file needs to be edited.
	private boolean addSongs = false;
	private boolean deleteSongs = false;
	
	// Stores the file paths of the xml songs.
	// This is important if a song has to be removed from the xml file as it is used to find the node to remove. 
	private ArrayList<String> xmlSongsFilePaths = new ArrayList<String> ();
	
	// Stores files in the music directory.
	private ArrayList<File> musicDirFiles = new ArrayList<File> ();
	
	// Initializes array lists to store the file names of the songs in the xml file
	// and the filenames of the songs in the music directory.
	// These array lists will be checked to determine if a song has been added or deleted from the music directory.
	private ArrayList<String> xmlSongsFileNames = new ArrayList<String> ();
	private ArrayList<String> musicDirFileNames = new ArrayList<String> ();

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
	    // Creates a task that is used to update the music library.
        ImportMusicTask<Boolean> task = new ImportMusicTask<Boolean>() {
        	@Override protected Boolean call() throws Exception {
        		try {
            		// Finds the file name of the songs in the library xml file and 
        			// stores them in the xmlSongsFileNames array list.
					xmlSongsfileNameFinder();
					
					// TODO: DEBUG
					System.out.println("UMDC_86: xmlSongTitleFinder size = " + xmlSongsFileNames.size());
					
            		// Finds the song titles in the music directory and stores them in the librarySongs array list.
					musicDirFileFinder(new File(musicDirectory));
					
					// TODO: DEBUG
					System.out.println("UMDC_92: musicDirFiles size = " + musicDirFiles.size() + " | musicDirFileNames size = " + musicDirFileNames.size());
					
					// Initializes a counter variable to index the xmlSongsFilePaths array to get the
					// file path of the songs that need to be removed from the xml file.
					int i = 0;
					// Loops through xmlSongsFileNames and checks if all the xml songs are in the music directory.
					// If one of the songs in the xml file is not in the music directory, then it was DELETED.
					for (String songFileName : xmlSongsFileNames) {
						// If the songFileName is not in the musicDirFileNames,
						// then it was deleted from the music directory and needs to be deleted from the xml file.
						if (!musicDirFileNames.contains(songFileName)) {
							// Adds the songs that need to be deleted to the array list in XMLEditor.
							XMLEditor.addSongPathsToDelete(xmlSongsFilePaths.get(i));
							deleteSongs = true;
						}
						i++;
					}
					
					
					// TODO: CHECK ADD SCENARIO
					
					// Initializes counter variable to increment xml song size for new songs that will be added.
					// This prevents a problem where if more than one song has been added to the library,
					// they would all have the same id = xmlSong.size() + 1
					int j = 0;
					// Initializes a counter variable to index the musicDirFiles array to get the file
					// corresponding to the song that needs to be added to the xml file.
					int k = 0;
					// Loops through musicDirFiles and checks if the song file names are in the library.xml file. 
					// If not, then the song needs to be ADDED.
					for (String songFileName : musicDirFileNames) {
						// If the song file name is not in the xmlSongsFilenames,
						// then it was added to the music directory and needs to be added to the xml file.
						if (!xmlSongsFileNames.contains(songFileName)) {
							// Adds the new song the library new songs array.
							XMLEditor.createNewSongObject(musicDirFiles.get(k), xmlSongsFileNames.size() + j);
							j++;
							addSongs = true;
						}
						k++;
					}
					
					// If a song needs to be added to the xml file.
					if (addSongs) {	
			            // Adds the new song to the xml file.
						XMLEditor.addSongToXML();
					}
					
		            // If a song needs to be deleted from the xml file.
					if (deleteSongs) {
						// Deletes song from library xml file.
						XMLEditor.deleteSongFromXML();
					}
					
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
	
	private void xmlSongsfileNameFinder() {
		try {
			// Creates reader for xml file.
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true);
			FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
			XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = null;
			String songLocation;
			
			// Loops through xml file looking for song titles.
			// Stores the song title in the xmlSongsFileNames array list.
			while(reader.hasNext()) {
			    reader.next();
			    if (reader.isWhiteSpace()) {
			        continue;
			    } else if (reader.isStartElement()) {
			    	element = reader.getName().getLocalPart();
			    } else if (reader.isCharacters() && element.equals("location")) {
			    	// Retrieves the song location and adds it to the corresponding array list.
			    	songLocation = reader.getText();
			    	xmlSongsFilePaths.add(songLocation);
			    	
			    	// Retrieves the file name from the file path and adds it to the xmlSongsFileNames array list.
			    	int i = songLocation.lastIndexOf("\\");
			    	String songFileName = songLocation.substring(i + 1, songLocation.length());
			    	xmlSongsFileNames.add(songFileName);
			    }
			}
			// Closes xml reader.
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void musicDirFileFinder(File musicDirectoryFile) {
    	// Lists all the files in the music directory and stores them in an array.
        File[] files = musicDirectoryFile.listFiles();

        // Loops through the files.
        for (File file : files) {
            if (file.isFile() && Library.isSupportedFileType(file.getName())) {
            	// Adds the file to the musicDirFiles array list. 
            	musicDirFiles.add(file);
            	
            	// Adds the file name to the musicDirFileNames array list.
            	musicDirFileNames.add(file.getName());
            } else if (file.isDirectory()) {
            	musicDirFileFinder(file);
            }
        }
	}
}

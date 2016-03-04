package app.musicplayer.util;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import app.musicplayer.model.Song;

public class DirectoryWatch {
	
	private WatchService watcher;
	private Map<WatchKey, Path> keys;
	
	private boolean trace = false;
	
	private boolean fileCreated;
	private boolean fileDeleted;
	
	private String path;
	
	// Initializes the variable to store the number of files in library.xml file.
	// This is used to set the id of the new songs being added to library.xml
	private int xmlFileNum;
	
	// Array list with new songs to be added to library.xml
	private static ArrayList<Song> newSongs = new ArrayList<Song>();
	
	/**
	 * Creates a Directory Watch object.
	 */
	public DirectoryWatch(Path musicDirectory, int xmlFileNum) {
		// Sets the number of files in library.xml
		this.xmlFileNum = xmlFileNum;
		try {
			// Creates new watch service to monitor directory.
			watcher = FileSystems.getDefault().newWatchService();
			keys = new HashMap<WatchKey, Path>();
			
			// Registers music directory and all sub directories with watch service.
			registerAll(musicDirectory);
			
			// Enable trace after initial registration.
			this.trace = true;
			
			// TODO: DEBUG
			System.out.println("DW66_Watch Service reigstered for directory: " + musicDirectory.getFileName() + 
					" in: " + musicDirectory.getParent());
			
			// Sets infinite loop to monitor directory.
			while (true) {
				// Waits for the key to be signaled.
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
				Path dir = keys.get(key);
				if (dir == null) {
					System.err.println("Watchkey not recognized!");
					continue;
				}
				
				for (WatchEvent<?> event: key.pollEvents()) {
//					Thread.sleep(500);
					// Gets event type (create, delete, modify).
					WatchEvent.Kind<?> kind = event.kind();
					
					// Gets file name of file that triggered event.
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = ev.context();
					Path child = dir.resolve(fileName);

					// 	TODO: DEBUG
	                System.out.format("DW97_%s: %s\n", kind.name(), child);
					
					// TODO: CALL METHODS TO DEAL WITH THESE CASES
					// If directory is created, register directory and sub directories.
					if (kind == ENTRY_CREATE) {
						try {
							if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
								registerAll(child);
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						// TODO: RUN IN NEW THREAD?
						// Adds the new created songs to the new songs array list.
						fileCreate(child);
						
					} else if (kind == ENTRY_DELETE) {
						System.out.println("DW118_File deleted!");
					}
				}
				
				// TODO: EDIT XML FILE, UPDATE SONGS ARRAY LIST IN LIBRARY
				// Writes the new songs to the xml file once all the watch events have been analyzed.
				if (fileCreated) {
					editCreateXMLFile();
				} else if (fileDeleted) {
					editDeleteXMLFile();
				}
				
				// Resets the key.
				// If the key is no longer valid, exits loop.
				// Makes it possible to receive further watch events.
				boolean valid = key.reset();
				if (!valid) {
					keys.remove(key);
					// If all directories are inaccessible.
					if (keys.isEmpty()){
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getPath() {
		return this.path;
	}
	
	private void registerAll(final Path start) throws IOException {
		// Registers directory and all its sub-directories with the WatchService.
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
	}
	
	private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("DW160_Register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("DW163_Update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
	}
	
	private void fileCreate(Path filePath) {
		
		// TODO: DEBUG
		System.out.println("DW173_File Path: " + filePath);
		
		File file = filePath.toFile();
		
		System.out.println("DW177_File: " + file);
		
		if (file.isFile()) {
			System.out.println("DW180_New file created: " + file.getName());
			
            try {
            	
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();
                AudioHeader header = audioFile.getAudioHeader();
                
                // Gets song properties.
                int id = xmlFileNum++;
                String title = tag.getFirst(FieldKey.TITLE);
                // Gets the artist, empty string assigned if song has no artist.
                String artistTitle = tag.getFirst(FieldKey.ALBUM_ARTIST);
                if (artistTitle == null || artistTitle.equals("") || artistTitle.equals("null")) {
                    artistTitle = tag.getFirst(FieldKey.ARTIST);
                }
                String artist = (artistTitle == null || artistTitle.equals("") || artistTitle.equals("null")) ? "" : artistTitle;
                String album = tag.getFirst(FieldKey.ALBUM);
                // Gets the track length (as an int), converts to long and saves it as a duration object.                
                Duration length = Duration.ofSeconds(Long.valueOf(header.getTrackLength()));
                // Gets the track number and converts to an int. Assigns 0 if a track number is null.
                String track = tag.getFirst(FieldKey.TRACK);                
                int trackNumber = Integer.parseInt((track == null || track.equals("") || track.equals("null")) ? "0" : track);
                // Gets disc number and converts to int. Assigns 0 if the disc number is null.
                String disc = tag.getFirst(FieldKey.DISC_NO);
                int discNumber = Integer.parseInt((disc == null || disc.equals("") || disc.equals("null")) ? "0" : disc);
                int playCount = 0;
                LocalDateTime playDate = LocalDateTime.now();
                String location = Paths.get(file.getAbsolutePath()).toString();
                
                // TODO: DEBUG
                System.out.println("ID: " + id);
                System.out.println("Title: " + title);
                System.out.println("Artist: " + artist);
                System.out.println("Album: " + album);
                System.out.println("Length: " + length);
                System.out.println("Track Number: " + trackNumber);
                System.out.println("Disc Number: " + discNumber);
                System.out.println("Play Count: " + playCount);
                System.out.println("Play Date: " + playDate);
                System.out.println("Location: " + location);
                // TODO: DEBUG
                
                // Creates a new song object for the added song and adds it to the newSongs array list.
                newSongs.add(new Song(id, title, artist, album, length, trackNumber, discNumber, playCount, playDate, location));
                fileCreated = true;
                
                // TODO: DEBUG
                System.out.println("DW227_New song added to newSongs");
                // TODO: DEBUG

			} catch (Exception ex) {
				ex.printStackTrace();
			}
            
//            Platform.runLater(() -> {
//            	MusicPlayer.updateLibraryXML(filePath.getParent());
//            });
			
			// Creates a new update music dialog and sets the path to the new file.
//			UpdateMusicDialogController updateDialog = new UpdateMusicDialogController();
//			updateDialog.setMusicDirectory(filePath);
//			updateDialog.handleUpdate();
            
		} else if (file.isDirectory()) {
			System.out.println("DW244_New folder created!");
		} else {
			System.out.println("Nothing happened.");
		}
	}
	
	private void editCreateXMLFile() {}
	
	private void editDeleteXMLFile() {}

}

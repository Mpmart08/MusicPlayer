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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class DirectoryWatch {
	
	private WatchService watcher;
	private Map<WatchKey, Path> keys;
	
	private String path;
	private boolean trace = false;
	
	/**
	 * Creates a Directory Watch object.
	 */
	public DirectoryWatch(Path musicDirectory) {
		try {
			// Creates new watch service to monitor directory.
			watcher = FileSystems.getDefault().newWatchService();
			keys = new HashMap<WatchKey, Path>();
			
			// Registers music directory and all sub directories with watch service.
			registerAll(musicDirectory);
			
			// Enable trace after initial registration.
			this.trace = true;
			
			// TODO: DEBUG
			System.out.println("DW45_Watch Service reigstered for directory: " + musicDirectory.getFileName() + 
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
					// Gets event type (create, delete, modify).
					WatchEvent.Kind<?> kind = event.kind();
					
					// Gets file name of file that triggered event.
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = ev.context();
					Path child = dir.resolve(fileName);

					// 	TODO: DEBUG
	                System.out.format("DW76_%s: %s\n", kind.name(), child);
					
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
						fileAdd(child);
					} else if (kind == ENTRY_DELETE) {
						System.out.println("DW90_File deleted!");
					}
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
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
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
                System.out.format("DW133_Register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("DW136_Update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
	}
	
	private void fileAdd(Path filePath) {
		// TODO: RETURN SONG PROPERTIES
		
		// TODO: DEBUG
		System.out.println("DW147_File Path: " + filePath);
		
		File file = filePath.toFile();
		
		System.out.println("DW151_File: " + file);
		
		if (file.isFile()) {
			System.out.println("DW156_New file created!");
            
//            Platform.runLater(() -> {
//            	MusicPlayer.updateLibraryXML(filePath.getParent());
//            });
			
			// Creates a new update music dialog and sets the path to the new file.
//			UpdateMusicDialogController updateDialog = new UpdateMusicDialogController();
//			updateDialog.setMusicDirectory(filePath);
//			updateDialog.handleUpdate();
			
//            try {
//				AudioFile audioFile = AudioFileIO.read(file);
//				Tag tag = audioFile.getTag();
//				AudioHeader header = audioFile.getAudioHeader();
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
			
		} else if (file.isDirectory()) {
			System.out.println("DW156_New folder created!");
		} else {
			System.out.println("Nothing happened.");
		}
		
	}

}

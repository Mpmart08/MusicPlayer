package app.musicplayer.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirectoryWatch {
	private String path;
	
	/**
	 * Creates a Directory Watch object.
	 */
	public DirectoryWatch(Path musicDirectory) {
		try {
			// Creates new watch service to monitor directory.
			WatchService watcher = FileSystems.getDefault().newWatchService();
			
            // Watches for file creation, deletion, or modification.
			musicDirectory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			
			// TODO: DEBUG
			System.out.println("DW29_Watch Service reigstered for directory: " + musicDirectory.getFileName() + 
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
				
				for (WatchEvent<?> event: key.pollEvents()) {
					// Gets event type (create, delete, modify).
					WatchEvent.Kind<?> kind = event.kind();
					
					// Gets file name of file that triggered event.
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = ev.context();

					// 	TODO: DEBUG
					System.out.println("DW53_File Name to String: " + fileName.toString());
					System.out.println("DW54_" + kind.name() + ": " + fileName);
					
					// TODO: CALL METHODS TO DEAL WITH THESE CASES
					if (kind == ENTRY_CREATE) {
						fileAdd(fileName);
					} else if (kind == ENTRY_DELETE) {
						System.out.println("File deleted!");
					} else if (kind == ENTRY_MODIFY) {
						System.out.println("File modified!");
					}
				}
				// Resets the key.
				// If the key is no longer valid, exits loop.
				// Makes it possible to receive further watch events.
				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getPath() {
		return this.path;
	}
	
	private void fileAdd(Path fileName) {
		System.out.println("File created!");
		
		// TODO: RETURN SONG PROPERTIES
		System.out.println("DW86_File Name: " + fileName);
		
		File newFile = fileName.toFile();
		
		if (newFile.isFile()) {
			System.out.println("DW91_New file created!");
		} else if (newFile.isDirectory()) {
			System.out.println("DW93_New folder created!");
		}
		
	}

}

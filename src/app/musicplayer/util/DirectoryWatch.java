package app.musicplayer.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class DirectoryWatch {
	private String path;
	
	/**
	 * Creates a Directory Watch object.
	 */
	public DirectoryWatch() {
		try {
			// Creates new watch service to monitor directory.
			WatchService watcher = FileSystems.getDefault().newWatchService();
			
			// Creates reader for xml file.
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
            XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
            
            String element = null;
            
            // Loops through xml file looking for the music directory file path.
            while(reader.hasNext()) {
                reader.next();
                if (reader.isWhiteSpace()) {
                    continue;
                } else if (reader.isStartElement()) {
                	element = reader.getName().getLocalPart();
                	
                	// TODO: DEBUG 
                    System.out.println("Start Element: " + element);
                    
                } else if (reader.isCharacters() && element.equals("musicLibrary")) {
                	path = reader.getText();
                	
                	// TODO: DEBUG
                	System.out.println("Path: " + path);
                	
                	break;
                }
            }
            // Closes xml reader.
            reader.close();
            
            // Gets the directory and watches for file creation, deletion, or modification.
			Path directory = Paths.get(path);
			directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			
			// TODO: DEBUG
			System.out.println("Watch Service reigstered for directory: " + directory.getFileName());
			
			// Sets infinite loop to monitor directory.
			while (true) {
				WatchKey key;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

package app.musicplayer.util;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Song;

public class XMLEditor {
	
	// Initializes array list with song files of songs to be added to library.xml
	private static ArrayList<File> songFilesToAdd = new ArrayList<File>();	
	
	// Initializes array list with song paths of songs to be deleted from library.xml
	private static ArrayList<String> songPathsToDelete = new ArrayList<String>();
	
	public static void addSongFilesToAdd(File songFile) {
		songFilesToAdd.add(songFile);
	}
	
	public static void addSongPathsToDelete(String song) {
		songPathsToDelete.add(song);
	}
	
	public static void addSongToXML() {
		
		// Initializes the array list with song objects to add to the xml file.
		ArrayList<Song> songsToAdd = createNewSongObject();
		
        try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(Resources.JAR + "library.xml");
			
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            // Creates node to add songs.
            XPathExpression expr = xpath.compile("/library/songs");
            Node songsNode = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
            
            // Loops through the songs in the new song array list and adds them to the xml file.
            for (Song song : songsToAdd) {
                // Creates a new song element and its sub elements.
                Element newSong = doc.createElement("song");
                Element newSongId = doc.createElement("id");
                Element newSongTitle = doc.createElement("title");
                Element newSongArtist = doc.createElement("artist");
                Element newSongAlbum = doc.createElement("album");
                Element newSongLength = doc.createElement("length");
                Element newSongTrackNumber = doc.createElement("trackNumber");
                Element newSongDiscNumber = doc.createElement("discNumber");
                Element newSongPlayCount = doc.createElement("playCount");
                Element newSongPlayDate = doc.createElement("playDate");
                Element newSongLocation = doc.createElement("location");

                // Saves the new song data.
                newSongId.setTextContent(Integer.toString(song.getId()));
                newSongTitle.setTextContent(song.getTitle());
                newSongArtist.setTextContent(song.getArtist());
                newSongAlbum.setTextContent(song.getAlbum());
                newSongLength.setTextContent(Long.toString(song.getLengthInSeconds()));
                newSongTrackNumber.setTextContent(Integer.toString(song.getTrackNumber()));
                newSongDiscNumber.setTextContent(Integer.toString(song.getDiscNumber()));
                newSongPlayCount.setTextContent(Integer.toString(song.getPlayCount()));
                newSongPlayDate.setTextContent(song.getPlayDate().toString());
                newSongLocation.setTextContent(song.getLocation());
                
                // Adds the new song to the xml file.
                songsNode.appendChild(newSong);
                // Adds the new song data to the new song.
                newSong.appendChild(newSongId);
                newSong.appendChild(newSongTitle);
                newSong.appendChild(newSongArtist);
                newSong.appendChild(newSongAlbum);
                newSong.appendChild(newSongLength);
                newSong.appendChild(newSongTrackNumber);
                newSong.appendChild(newSongDiscNumber);
                newSong.appendChild(newSongPlayCount);
                newSong.appendChild(newSongPlayDate);
                newSong.appendChild(newSongLocation);
            }
            
            // Calculates the new xml file number, taking into account the new songs.
            int newXMLFileNum = MusicPlayer.getXMLFileNum() + songFilesToAdd.size();

            // Creates node to update xml file number.
            expr = xpath.compile("/library/musicLibrary/fileNum");
            Node fileNum = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
            
            // Updates the fileNum field in the xml file.
            fileNum.setTextContent(Integer.toString(newXMLFileNum));
            // Updates the xmlFileNum in MusicPlayer. 
            MusicPlayer.setXMLFileNum(newXMLFileNum);
            
            // Gets 
            int newLastIdAssigned = songsToAdd.get(songsToAdd.size() - 1).getId();
            
            // Creates node to update xml last id assigned.
            expr = xpath.compile("/library/musicLibrary/lastId");
            Node lastId = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
            
            // Updates the last id in the xml file.
            lastId.setTextContent(Integer.toString(newLastIdAssigned));
            // Updates the lastId in MusicPlayer.
        	MusicPlayer.setLastIdAssigned(newLastIdAssigned);
            
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            File xmlFile = new File(Resources.JAR + "library.xml");
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
            
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void deleteSongFromXML() {
		// Gets the currentXMLFileNum.
		int currentXMLFileNum = MusicPlayer.getXMLFileNum();

        try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(Resources.JAR + "library.xml");
			
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            // Retrieves the last id assigned to a song from the xml file.
            int xmlLastIdAssigned = xmlLastIdAssignedFinder();

            // Finds the song node corresponding to the last assigned id.
            XPathExpression expr = xpath.compile("/library/songs/song[id/text() = \"" + xmlLastIdAssigned + "\"]");
            Node lastSongNode = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
            
            // Loops through the songPathsToDelete array list and removes the nodes from the xml file.
            Node deleteSongNode = null;
            for (String songFilePath : songPathsToDelete) {
                // Finds the node with the song title marked for removal.
            	expr = xpath.compile("/library/songs/song[location/text() = \"" + songFilePath + "\"]");
                deleteSongNode = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
                
                // Removes the node corresponding to the title of the song.
                deleteSongNode.getParentNode().removeChild(deleteSongNode);

            	// Decreases the counter for the number of files in the xml file.
                currentXMLFileNum--;
            }
            
            // If the last node to be deleted was the last song node,
            // then the new last assigned id is found and updated in the MusicPlayer and xml file.
            if (deleteSongNode == lastSongNode) {
            	int newLastIdAssigned = xmlNewLastIdAssignedFinder();

                // Creates node to update xml last id assigned.
                expr = xpath.compile("/library/musicLibrary/lastId");
                Node lastId = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
                
                // Updates the lastId in MusicPlayer and in the xml file.
            	MusicPlayer.setLastIdAssigned(newLastIdAssigned);
                lastId.setTextContent(Integer.toString(newLastIdAssigned));
            }
            
            // Creates node to update xml file number.
            XPathExpression fileNumExpr = xpath.compile("/library/musicLibrary/fileNum");
            Node fileNum = ((NodeList) fileNumExpr.evaluate(doc, XPathConstants.NODESET)).item(0);
            
            // Updates the fileNum in MusicPlayer and in the xml file.
            MusicPlayer.setXMLFileNum(currentXMLFileNum);
            fileNum.setTextContent(Integer.toString(currentXMLFileNum));
                    
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            File xmlFile = new File(Resources.JAR + "library.xml");
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
            
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void deleteSongFromPlaylist(int selectedPlayListId, int selectedSongId) {
        try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(Resources.JAR + "library.xml");
			
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            // Finds the node with the song id for the selected song in the selected play list for removal.
            String query = "/library/playlists/playlist[@id='" + selectedPlayListId + "']/songId[text() = '" + selectedSongId + "']";
            XPathExpression expr = xpath.compile(query);
            Node deleteSongNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            
            // Removes the node corresponding to the selected song.
            deleteSongNode.getParentNode().removeChild(deleteSongNode);
                    
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            File xmlFile = new File(Resources.JAR + "library.xml");
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
            
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void deletePlaylistFromXML(int selectedPlayListId) {		
        try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(Resources.JAR + "library.xml");
			
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            // Finds the node with the play list id for removal.
            String query = "/library/playlists/playlist[@id='" + selectedPlayListId + "']";
            XPathExpression expr = xpath.compile(query);
            Node deleteplaylistNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            
            // Removes the node corresponding to the selected song.
            deleteplaylistNode.getParentNode().removeChild(deleteplaylistNode);
                    
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            File xmlFile = new File(Resources.JAR + "library.xml");
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
            
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static ArrayList<Song> createNewSongObject() {
		
		ArrayList<Song> newSongObjects = new ArrayList<>();
		
		// Searches the xml file to get the last id assigned.
		int lastIdAssigned = xmlLastIdAssignedFinder();
		
		// Loops through each song file that needs to be added and creates a song object for each.
		// Each song object is added to an array list and returned so that they can be added to the xml file.
		for (File songFile : songFilesToAdd) {
	        try {
	            AudioFile audioFile = AudioFileIO.read(songFile);
	            Tag tag = audioFile.getTag();
	            AudioHeader header = audioFile.getAudioHeader();
	            
	            // Gets song properties.
	            int id = ++lastIdAssigned;
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
	            String location = Paths.get(songFile.getAbsolutePath()).toString();
	            
	            // Creates a new song object for the added song and adds it to the newSongs array list.
	            Song newSong = new Song(id, title, artist, album, length, trackNumber, discNumber, playCount, playDate, location);
	            
	            // Adds the new song to the songsToAdd array list.
	            newSongObjects.add(newSong);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// Updates the lastIdAssigned in MusicPlayer to account for the new songs.
		MusicPlayer.setLastIdAssigned(lastIdAssigned);
		
		// Returns the array list with all the song objects so that they can be added to the xml file.
		return newSongObjects;
	}
	
    private static int xmlLastIdAssignedFinder() {
		try {
			// Creates reader for xml file.
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true);
			FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
			XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = null;
			String lastId = null;
			
			// Loops through xml file looking for the music directory file path.
			while(reader.hasNext()) {
			    reader.next();
			    if (reader.isWhiteSpace()) {
			        continue;
			    } else if (reader.isStartElement()) {
			    	element = reader.getName().getLocalPart();
			    } else if (reader.isCharacters() && element.equals("lastId")) {
			    	lastId = reader.getText();               	
			    	break;
			    }
			}
			// Closes xml reader.
			reader.close();
			
			// Converts the file number to an int and returns the value. 
			return Integer.parseInt(lastId);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
    }
    
    private static int xmlNewLastIdAssignedFinder() {
		try {
			// Creates reader for xml file.
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true);
			FileInputStream is = new FileInputStream(new File(Resources.JAR + "library.xml"));
			XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");
			
			String element = null;
			String location = null;
			
			String currentSongId = null;
			String xmlNewLastIdAssigned = null;
			
			// Loops through xml file looking for the music directory file path.
			while(reader.hasNext()) {
			    reader.next();
			    if (reader.isWhiteSpace()) {
			        continue;
			    } else if (reader.isStartElement()) {
			    	element = reader.getName().getLocalPart();
			    } else if (reader.isCharacters() && element.equals("id")) {
			    	currentSongId = reader.getText();
			    } else if (reader.isCharacters() && element.equals("location")) {
			    	location = reader.getText();
			    	// If the current location is does not correspond to one of the files to be deleted,
			    	// then the current id is assigned as the newLastIdAssigned.
			    	if (!songPathsToDelete.contains(location)) {
			    		xmlNewLastIdAssigned = currentSongId;
			    	}
			    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("songs")) {
			    	break;
			    }
			}
			// Closes xml reader.
			reader.close();
			
			// Converts the file number to an int and returns the value. 
			return Integer.parseInt(xmlNewLastIdAssigned);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
    }
}

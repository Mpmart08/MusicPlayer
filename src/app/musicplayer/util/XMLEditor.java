package app.musicplayer.util;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

import app.musicplayer.model.Library;
import app.musicplayer.model.Song;

public class XMLEditor {
	
	// Initializes the variable to store the number of files in library.xml
	// This is used to set the id of the new songs being added to library.xml
	private static int xmlFileNum;
	
	// Initializes array list with song titles of songs to be deleted from library.xml
	private static ArrayList<String> songsToDelete = new ArrayList<String>();
	
	public static ArrayList<String> getSongsToDelete() {
		return songsToDelete;
	}
	
	public static void createNewSongObject(File file, int fileNum) {
		// Sets the number of files in library.xml
		xmlFileNum = fileNum;
		
		// Infinite loop to wait until file is not in use by another process.
		while (!file.renameTo(file)) {}
		
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
            
            // Creates a new song object for the added song and adds it to the newSongs array list.
            Song newSong = new Song(id, title, artist, album, length, trackNumber, discNumber, playCount, playDate, location);
            
            // Adds the new song to the new songs array list in Library.
            Library.addNewSong(newSong);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void addSongToXML() {		
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
            for (Song song : Library.getNewSongs()) {
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
            
            // Creates node to update xml file number.
            expr = xpath.compile("/library/musicLibrary/fileNum");
            Node fileNum = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
            
            // Updates the fileNum field in the xml file.
            fileNum.setTextContent(Integer.toString(xmlFileNum));
            
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
	
	public static void deleteSongFromXML(int currentXMLFileNum) {
		// Sets the current number of files in library.xml
		xmlFileNum = currentXMLFileNum;
		
        try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(Resources.JAR + "library.xml");
			
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            for (String songTitle : songsToDelete) {
                // Finds the node with the song title marked for removal.
            	XPathExpression expr = xpath.compile("/library/songs/song[title/text() = \"" + songTitle + "\"]");
                Node deleteSongNode = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);
                
                // Removes the node corresponding to the title of the song.
                deleteSongNode.getParentNode().removeChild(deleteSongNode);

            	// Decreases the counter for the number of files in the xml file.
            	xmlFileNum--;
            }
            
            // Creates node to update xml file number.
            XPathExpression fileNumExpr = xpath.compile("/library/musicLibrary/fileNum");
            Node fileNum = ((NodeList) fileNumExpr.evaluate(doc, XPathConstants.NODESET)).item(0);
            
            // Updates the fileNum field in the xml file.
            fileNum.setTextContent(Integer.toString(xmlFileNum));
                    
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
		
		// TODO: DEBUG
		System.out.println("XMLE_216: In deleteSongFromPlaylist");
		System.out.println("XMLE_217: selected playlist: " + selectedPlayListId + " selected song: " + selectedSongId);
		
        try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(Resources.JAR + "library.xml");
			
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            // Finds the node with the song id for the selected song in the selected play list for removal.
            String query = "/library/playlists/playlist[@id='" + selectedPlayListId + "']/songs/songId[text() = '" + selectedSongId + "']";
            XPathExpression expr = xpath.compile(query);
            Node deleteSongNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            
            // Removes the node corresponding to the title of the song.
            deleteSongNode.getParentNode().removeChild(deleteSongNode);
            
            System.out.println("XMLE_230: " + deleteSongNode.getNodeName());
                    
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
}

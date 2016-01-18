package app.musicplayer.model;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import app.musicplayer.util.ImportMusicTask;
import app.musicplayer.util.Resources;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Library {

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String ARTIST = "artist";
    private static final String ALBUM = "album";
    private static final String LENGTH = "length";
    private static final String TRACKNUMBER = "trackNumber";
    private static final String DISCNUMBER = "discNumber";
    private static final String PLAYCOUNT = "playCount";
    private static final String PLAYDATE = "playDate";
    private static final String LOCATION = "location";
    private static final String SONGID = "songId";

    private static ObservableList<Song> songs;
    private static ObservableList<Artist> artists;
    private static ObservableList<Album> albums;
    private static ObservableList<Playlist> playlists;

    /**
     * Gets a list of songs.
     * @return observable list of songs
     */
    public static ObservableList<Song> getSongs() {
    	// If the observable list of songs has not been initialized.
        if (songs == null) {
            songs = FXCollections.observableArrayList();
            
            try {

                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty("javax.xml.stream.isCoalescing", true);
                FileInputStream is = new FileInputStream(new File(Resources.XML + "library.xml"));
                XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");

                String element = "";
                int id = -1;
                String title = null;
                String artist = null;
                String album = null;
                Duration length = null;
                int trackNumber = -1;
                int discNumber = -1;
                int playCount = -1;
                LocalDateTime playDate = null;
                String location = null;

                while(reader.hasNext()) {
                    reader.next();

                    if (reader.isWhiteSpace()) {
                        continue;
                    } else if (reader.isCharacters()) {
                        String value = reader.getText();
                        
                        switch (element) {
                            case ID:
                                id = Integer.parseInt(value);
                                break;
                            case TITLE:
                                title = value;
                                break;
                            case ARTIST:
                                artist = value;
                                break;
                            case ALBUM:
                                album = value;
                                break;
                            case LENGTH:
                                length = Duration.ofSeconds(Long.parseLong(value));
                                break;
                            case TRACKNUMBER:
                                trackNumber = Integer.parseInt(value);
                                break;
                            case DISCNUMBER:
                                discNumber = Integer.parseInt(value);
                                break;
                            case PLAYCOUNT:
                                playCount = Integer.parseInt(value);
                                break;
                            case PLAYDATE:
                                playDate = LocalDateTime.parse(value);
                                break;
                            case LOCATION:
                                location = value;
                                break;
                        } // End switch
                    } else if (reader.isStartElement()) {
                    	
                        element = reader.getName().getLocalPart();
                        
                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("song")) {

                        songs.add(new Song(id, title, artist, album, length, trackNumber, discNumber, playCount, playDate, location));
                        id = -1;
                        title = null;
                        artist = null;
                        album = null;
                        length = null;
                        trackNumber = -1;
                        discNumber = -1;
                        playCount = -1;
                        playDate = null;
                        location = null;

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("songs")) {

                        reader.close();
                        break;
                    }
                } // End while
                
                reader.close();
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // End if (songs == null)
        return songs;
    }
    
    /**
     * Gets a list of albums.
     * 
     * @return observable list of albums
     */
    public static ObservableList<Album> getAlbums() {
    	// If the observable list of albums has not been initialized.
        if (albums == null) {
            if (songs == null) {
                getSongs();
            }
            
            albums = FXCollections.observableArrayList();
            
            TreeMap<String, List<Song>> albumMap = new TreeMap<String, List<Song>>(
                songs.stream()
                    .filter(song -> song.getAlbum() != null)
                    .collect(Collectors.groupingBy(Song::getAlbum))
            );
            
            int id = 0;
            
            for (Map.Entry<String, List<Song>> entry : albumMap.entrySet()) {
                ArrayList<Song> songs = new ArrayList<Song>();

                for (Song song : entry.getValue()) {
                    songs.add(song);
                }

                TreeMap<String, List<Song>> artistMap = new TreeMap<String, List<Song>>(
                    songs.stream()
                        .filter(song -> song.getArtist() != null)
                        .collect(Collectors.groupingBy(Song::getArtist))
                );

                for (Map.Entry<String, List<Song>> e : artistMap.entrySet()) {
                    ArrayList<Song> albumSongs = new ArrayList<Song>();
                    String artist = e.getValue().get(0).getArtist();

                    for (Song s : e.getValue()) {
                        albumSongs.add(s);
                    }
                    
                    albums.add(new Album(id++, entry.getKey(), artist, albumSongs));
                }
            }
        }
        return albums;
    } // End getAlbums()
    
    /**
     * Gets a list of artists.
     * 
     * @return observable list of artists
     */
    public static ObservableList<Artist> getArtists() {
    	
        if (artists == null) {
            if (albums == null) {
                getAlbums();
            }

            artists = FXCollections.observableArrayList();

            TreeMap<String, List<Album>> artistMap = new TreeMap<String, List<Album>>(
                albums.stream()
                    .filter(album -> album.getArtist() != null)
                    .collect(Collectors.groupingBy(Album::getArtist))
            );

            for (Map.Entry<String, List<Album>> entry : artistMap.entrySet()) {

                ArrayList<Album> albums = new ArrayList<Album>();

                for (Album album : entry.getValue()) {
                    albums.add(album);
                }

                artists.add(new Artist(entry.getKey(), albums));
            }
        }
        return artists;
    } // End getArtists()

    public static ObservableList<Playlist> getPlaylists() {

       if (playlists == null) {

            playlists = FXCollections.observableArrayList();

            int id = 0;
            playlists.add(new MostPlayedPlaylist(id++));
            playlists.add(new RecentlyPlayedPlaylist(id++));

            try {

                XMLInputFactory factory = XMLInputFactory.newInstance();
                FileInputStream is = new FileInputStream(new File(Resources.XML + "library.xml"));
                XMLStreamReader reader = factory.createXMLStreamReader(is, "UTF-8");

                String element = "";
                boolean isPlaylist = false;
                String title = null;
                ArrayList<Song> songs = new ArrayList<Song>();

                while(reader.hasNext()) {

                    reader.next();

                    if (reader.isWhiteSpace()) {

                        continue;

                    } else if (reader.isCharacters() && isPlaylist) {

                        String value = reader.getText();

                        switch (element) {

                            case ID:
                                id = Integer.parseInt(value);
                                break;
                            case TITLE:
                                title = value;
                                break;
                            case SONGID:
                                songs.add(getSong(Integer.parseInt(value)));
                                break;
                        }

                    } else if (reader.isStartElement()) {

                        element = reader.getName().getLocalPart();
                        if (element == "playlists") {
                            isPlaylist = true;
                        }

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("playlist")) {

                        playlists.add(new Playlist(id, title, songs));
                        id = -1;
                        title = null;
                        songs = new ArrayList<Song>();

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("playlists")) {

                        reader.close();
                        break;
                    }
                }
                
                reader.close();

            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }
        return playlists;
    }
    
    // GETTERS

    /**
     * Gets artists based on id number.
     * 
     * @param id
     * @return artists
     */
    public static Artist getArtist(int id) {
        if (artists == null) {
            getArtists();
        }
        return artists.get(id);
    }

    public static Album getAlbum(int id) {
        if (albums == null) {
            getAlbums();
        }
        return albums.get(id);
    }

    public static Song getSong(int id) {
        if (songs == null) {
            getSongs();
        }
        return songs.get(id);
    }

    public static Artist getArtist(String title) {
        if (artists == null) {
            getArtists();
        }
        return artists.stream().filter(artist -> title.equals(artist.getTitle())).findFirst().get();
    }

    public static Album getAlbum(String title) {
        if (albums == null) {
            getAlbums();
        }
        return albums.stream().filter(album -> title.equals(album.getTitle())).findFirst().get();
    }

    public static Song getSong(String title) {
        if (songs == null) {
            getSongs();
        }
        return songs.stream().filter(song -> title.equals(song.getTitle())).findFirst().get();
    }

    public static void importMusic(String path, ImportMusicTask<Boolean> task) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element library = doc.createElement("library");
        Element songs = doc.createElement("songs");
        Element playlists = doc.createElement("playlists");

        doc.appendChild(library);
        library.appendChild(songs);
        library.appendChild(playlists);

        File directory = new File(Paths.get(path).toUri());
        int maxProgress = getMaxProgress(directory, 0);

        writeXML(directory, doc, songs, 0, maxProgress, task);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        
        File xmlFile = new File(Resources.XML + "library.xml");
        
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(source, result);
    }
    
    private static int getMaxProgress(File directory, int maxProgress) {
    	
    	File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isFile()) {
            	maxProgress++;
            } else if (file.isDirectory()) {
            	maxProgress += getMaxProgress(file, 0);
            }
        }
        
        return maxProgress;
    }

    private static int writeXML(File directory, Document doc, Element songs, int i, int maxProgress, ImportMusicTask<Boolean> task) {

        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                try {
                    
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();
                    AudioHeader header = audioFile.getAudioHeader();

                    Element song = doc.createElement("song");
                    songs.appendChild(song);

                    Element id = doc.createElement("id");
                    Element title = doc.createElement("title");
                    Element artist = doc.createElement("artist");
                    Element album = doc.createElement("album");
                    Element length = doc.createElement("length");
                    Element trackNumber = doc.createElement("trackNumber");
                    Element discNumber = doc.createElement("discNumber");
                    Element playCount = doc.createElement("playCount");
                    Element playDate = doc.createElement("playDate");
                    Element location = doc.createElement("location");

                    id.setTextContent(Integer.toString(i++));
                    title.setTextContent(tag.getFirst(FieldKey.TITLE));
                    String artistTitle = tag.getFirst(FieldKey.ALBUM_ARTIST);
                    if (artistTitle == null || artistTitle.equals("") || artistTitle.equals("null")) {
                        artistTitle = tag.getFirst(FieldKey.ARTIST);
                    }
                    artist.setTextContent(
                        (artistTitle == null || artistTitle.equals("") || artistTitle.equals("null")) ? "" : artistTitle
                    );
                    album.setTextContent(tag.getFirst(FieldKey.ALBUM));
                    length.setTextContent(Integer.toString(header.getTrackLength()));
                    String track = tag.getFirst(FieldKey.TRACK);
                    trackNumber.setTextContent(
                        (track == null || track.equals("") || track.equals("null")) ? "0" : track
                    );
                    String disc = tag.getFirst(FieldKey.DISC_NO);
                    discNumber.setTextContent(
                        (disc == null || disc.equals("") || disc.equals("null")) ? "0" : disc
                    );
                    playCount.setTextContent("0");
                    playDate.setTextContent(LocalDateTime.now().toString());
                    location.setTextContent(Paths.get(file.getAbsolutePath()).toString());

                    song.appendChild(id);
                    song.appendChild(title);
                    song.appendChild(artist);
                    song.appendChild(album);
                    song.appendChild(length);
                    song.appendChild(trackNumber);
                    song.appendChild(discNumber);
                    song.appendChild(playCount);
                    song.appendChild(playDate);
                    song.appendChild(location);
                    
                    task.updateProgress(i, maxProgress);

                } catch (Exception ex) {
                    
                    ex.printStackTrace();
                }

            } else if (file.isDirectory()) {

                i = writeXML(file, doc, songs, i, maxProgress, task);
            }
        }

        return i;
    }
}
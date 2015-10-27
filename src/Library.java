package musicplayer;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public final class Library {

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String ARTIST = "artist";
    private static final String ALBUM = "album";
    private static final String LENGTH = "length";
    private static final String TRACKNUMBER = "trackNumber";
    private static final String PLAYCOUNT = "playCount";
    private static final String PLAYDATE = "playDate";
    private static final String LOCATION = "location";

    private static ObservableList<Song> songs = null;

    /*public List<Artist> getArtists() {
        
    }

    public List<Album> getAlbums() {
        
    }*/

    public static ObservableList<Song> getSongs() {

        if (songs == null) {

            songs = FXCollections.observableArrayList();

            try {

                XMLInputFactory factory = XMLInputFactory.newInstance();
                File library = new File("musicplayer\\" + Resources.XML + "library.xml");
                XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(library), "UTF-8");

                String element = "";
                int id = -1;
                String title = null;
                String artist = null;
                String album = null;
                Duration length = null;
                int trackNumber = -1;
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
                            case PLAYCOUNT:
                                playCount = Integer.parseInt(value);
                                break;
                            case PLAYDATE:
                                playDate = LocalDateTime.parse(value);
                                break;
                            case LOCATION:
                                location = value;
                                break;
                        }

                    } else if (reader.isStartElement()) {

                        element = reader.getName().getLocalPart();

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("song")) {

                        songs.add(new Song(id, title, artist, album, length, trackNumber, playCount, playDate, location));
                        id = -1;
                        title = null;
                        artist = null;
                        album = null;
                        length = null;
                        trackNumber = -1;
                        playCount = -1;
                        playDate = null;
                        location = null;

                    } else if (reader.isEndElement() && reader.getName().getLocalPart().equals("songs")) {

                        reader.close();
                        break;
                    }
                }

            } catch (Exception ex) {

                System.out.println(ex.getMessage());
            }
        }

        return songs;
    }

    /*public List<Playlist> getPlaylists() {

    }

    public List<Song> getSongsByArtist(String artist) {

    }

    public List<Song> getSongsByAlbum(String album) {
        
    }

    public List<Song> getSongsByPlaylist(String playlist) {
        
    }*/
}
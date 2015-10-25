package musicplayer;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.xml.stream.XMLInputFactory;
import java.io.FileInputStream;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public final class Song {

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String ARTIST = "artist";
    private static final String ALBUM = "album";
    private static final String LENGTH = "length";
    private static final String TRACKNUMBER = "trackNumber";
    private static final String PLAYCOUNT = "playCount";
    private static final String PLAYDATE = "playDate";
    private static final String LOCATION = "location";

    private int id;
    private String title;
    private String artist;
    private String album;
    private Duration length;
    private int trackNumber;
    private int playCount;
    private LocalDateTime playDate;
    private String location;
    private Image artwork;

    public Song(int id) {

        this.id = id;

        try {

            XMLInputFactory factory = XMLInputFactory.newInstance();
            File library = new File("musicplayer\\" + Resources.XML + "library.xml");
            XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(library), "UTF-8");

            boolean songFound = false;
            String element = "";

            while(reader.hasNext()) {

                reader.next();

                if (reader.isWhiteSpace()) {

                    continue;

                } else if (reader.isCharacters() && element.equals(ID)) {

                    int value =  Integer.parseInt(reader.getText());
                    songFound = (value == id);

                } else if (reader.isCharacters() && songFound) {

                    String value = reader.getText();

                    switch (element) {

                        case TITLE:
                            this.title = value;
                            break;
                        case ARTIST:
                            this.artist = value;
                            break;
                        case ALBUM:
                            this.album = value;
                            break;
                        case LENGTH:
                            this.length = Duration.ofSeconds(Long.parseLong(value));
                            break;
                        case TRACKNUMBER:
                            this.trackNumber = Integer.parseInt(value);
                            break;
                        case PLAYCOUNT:
                            this.playCount = Integer.parseInt(value);
                            break;
                        case PLAYDATE:
                            this.playDate = LocalDateTime.parse(value);
                            break;
                        case LOCATION:
                            this.location = value;
                            break;
                    }

                } else if (reader.isStartElement()) {

                    element = reader.getName().getLocalPart();

                } else if (reader.isEndElement() && songFound && reader.getName().getLocalPart().equals("song")) {

                    reader.close();
                    break;
                }
            }

        } catch (Exception ex) {

            System.out.println(ex.getMessage());

        }
    }

    public int getId() {

        return this.id;
    }

    public String getTitle() {

        return this.title;
    }

    public String getArtist() {

        return this.artist;
    }

    public String getAlbum() {

        return this.album;
    }

    public Duration getLength() {

        return this.length;
    }

    public int getTrackNumber() {

        return this.trackNumber;
    }

    public int getPlayCount() {

        return this.playCount;
    }

    public LocalDateTime getPlayDate() {

        return this.playDate;
    }

    public String getLocation() {

        return this.location;
    }

    public Image getArtwork() {

        if (artwork == null) {

            try {

                AudioFile audioFile = AudioFileIO.read(new File(location));
                Tag tag = audioFile.getTag();
                byte[] bytes = tag.getFirstArtwork().getBinaryData();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                artwork = new Image(in, 80, 80, true, false);

            } catch (Exception ex) {

                System.out.println(ex.getMessage());
            }
        }

        return artwork;
    }
}
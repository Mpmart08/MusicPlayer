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

public final class Song {

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

    public Song(int id, String title, String artist, String album, Duration length,
        int trackNumber, int playCount, LocalDateTime playDate, String location) {

        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.trackNumber = trackNumber;
        this.playCount = playCount;
        this.playDate = playDate;
        this.location = location;
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

    public String getLengthAsString() {

        long seconds = length.getSeconds() - (length.toMinutes() * 60);
        return length.toMinutes() + ":" + (seconds < 10 ? "0" + seconds : seconds);
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

    public Image getArtwork(int size) {

        if (artwork == null) {

            try {

                AudioFile audioFile = AudioFileIO.read(new File(location));
                Tag tag = audioFile.getTag();
                byte[] bytes = tag.getFirstArtwork().getBinaryData();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                artwork = new Image(in, size, size, true, true);

            } catch (Exception ex) {

                artwork = new Image(this.getClass().getResource(Resources.IMG + "albumsIcon.png").toString());
            }
        }

        return artwork;
    }

    public Image getArtwork() {

        return getArtwork(80);
    }
}
package app.musicplayer.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.musicplayer.util.Resources;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public final class Song implements Comparable<Song> {

    private int id;
    private SimpleStringProperty title;
    private SimpleStringProperty artist;
    private SimpleStringProperty album;
    private SimpleStringProperty length;
    private long lengthInSeconds;
    private int trackNumber;
    private int discNumber;
    private SimpleIntegerProperty playCount;
    private LocalDateTime playDate;
    private String location;
    private SimpleBooleanProperty playing;
    private SimpleBooleanProperty selected;

    /**
     * Constructor for the song class.
     *
     * @param id
     * @param title
     * @param artist
     * @param album
     * @param length
     * @param trackNumber
     * @param discNumber
     * @param playCount
     * @param playDate
     * @param location
     */
    public Song(int id, String title, String artist, String album, Duration length,
                int trackNumber, int discNumber, int playCount, LocalDateTime playDate, String location) {

        if (title == null) {
            Path path = Paths.get(location);
            String fileName = path.getFileName().toString();
            title = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        if (album == null) {
            album = "Unknown Album";
        }

        if (artist == null) {
            artist = "Unknown Artist";
        }

        this.id = id;
        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.lengthInSeconds = length.getSeconds();
        long seconds = length.getSeconds() % 60;
        this.length = new SimpleStringProperty(length.toMinutes() + ":" + (seconds < 10 ? "0" + seconds : seconds));
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.playCount = new SimpleIntegerProperty(playCount);
        this.playDate = playDate;
        this.location = location;
        this.playing = new SimpleBooleanProperty(false);
        this.selected = new SimpleBooleanProperty(false);
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title.get();
    }

    public StringProperty titleProperty() {
        return this.title;
    }

    public String getArtist() {
        return this.artist.get();
    }

    public StringProperty artistProperty() {
        return this.artist;
    }

    public String getAlbum() {
        return this.album.get();
    }

    public Image getArtwork() {
        return Library.getAlbum(this.album.get()).getArtwork();
    }

    public StringProperty albumProperty() {
        return this.album;
    }

    public String getLength() {
        return this.length.get();
    }

    public StringProperty lengthProperty() {
        return this.length;
    }

    public long getLengthInSeconds() {
        return this.lengthInSeconds;
    }

    public int getTrackNumber() {
        return this.trackNumber;
    }

    public int getDiscNumber() {
        return this.discNumber;
    }

    public int getPlayCount() {
        return this.playCount.get();
    }

    public IntegerProperty playCountProperty() {
        return this.playCount;
    }

    public LocalDateTime getPlayDate() {
        return this.playDate;
    }

    public String getLocation() {
        return this.location;
    }

    public BooleanProperty playingProperty() {
        return this.playing;
    }

    public boolean getPlaying() {
        return this.playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public BooleanProperty selectedProperty() {
        return this.selected;
    }

    public boolean getSelected() {
        return this.selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public void played() {
        this.playCount.set(this.playCount.get() + 1);
        this.playDate = LocalDateTime.now();

        Thread thread = new Thread(() -> {

            try {

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(Resources.JAR + "library.xml");

                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                XPathExpression expr = xpath.compile("/library/songs/song/playCount[../id/text() = \"" + this.id + "\"]");
                Node playCount = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);

                expr = xpath.compile("/library/songs/song/playDate[../id/text() = \"" + this.id + "\"]");
                Node playDate = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);

                playCount.setTextContent(Integer.toString(this.playCount.get()));
                playDate.setTextContent(this.playDate.toString());

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

        });

        thread.start();
    }

    @Override
    public int compareTo(Song other) throws NullPointerException {
        int discComparison = Integer.compare(this.discNumber, other.discNumber);

        if (discComparison != 0) {
            return discComparison;
        } else {
            return Integer.compare(this.trackNumber, other.trackNumber);
        }
    }
}
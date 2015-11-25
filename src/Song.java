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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPath;

public final class Song implements Comparable<Song> {

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
        getArtwork();
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

        long seconds = length.getSeconds() % 60;
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

    public Image getArtwork() {

        if (artwork == null) {

            try {

                AudioFile audioFile = AudioFileIO.read(new File(location));
                Tag tag = audioFile.getTag();
                byte[] bytes = tag.getFirstArtwork().getBinaryData();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                artwork = new Image(in, 300, 300, true, true);

            } catch (Exception ex) {

                artwork = new Image(this.getClass().getResource(Resources.IMG + "albumsIcon.png").toString());
            }
        }

        return artwork;
    }

    public void played() {

        this.playCount++;
        this.playDate = LocalDateTime.now();

        Thread thread = new Thread(() -> {

            try {

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse("musicplayer/" + Resources.XML + "library.xml");

                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                XPathExpression expr = xpath.compile("/library/songs/song/playCount[../id/text() = \"" + this.id + "\"]");
                Node playCount = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);

                expr = xpath.compile("/library/songs/song/playDate[../id/text() = \"" + this.id + "\"]");
                Node playDate = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0);

                playCount.setTextContent(Integer.toString(this.playCount));
                playDate.setTextContent(this.playDate.toString());

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(doc);
                File xmlFile = new File("musicplayer/" + Resources.XML + "library.xml");
                StreamResult result = new StreamResult(xmlFile);
                transformer.transform(source, result);

            } catch (Exception ex) {

                ex.printStackTrace();
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public int compareTo(Song other) throws NullPointerException {

        if (other == null) {

            throw new NullPointerException();
        }

        return Integer.compare(this.trackNumber, other.trackNumber);
    }
}
package musicplayer;

import java.util.ArrayList;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.net.URL;
import java.net.URLEncoder;
import java.io.File;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import java.io.ByteArrayInputStream;

public final class Album implements Comparable<Album> {

    private int id;
    private String title;
    private String artist;
    private Image artwork;
    private ArrayList<Song> songs;

    public Album(int id, String title, String artist, ArrayList<Song> songs) {

        this.id = id;
        this.title = title;
        this.artist = artist;
        this.songs = songs;
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

    public ArrayList<Song> getSongs() {

        return new ArrayList<Song>(this.songs);
    }

    public Image getArtwork() {

        if (this.artwork == null) {

            try {

                String location = this.songs.get(0).getLocation();
                AudioFile audioFile = AudioFileIO.read(new File(location));
                Tag tag = audioFile.getTag();
                byte[] bytes = tag.getFirstArtwork().getBinaryData();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                this.artwork = new Image(in, 300, 300, true, true);

            } catch (Exception ex) {

                this.artwork = new Image(this.getClass().getResource(Resources.IMG + "albumsIcon.png").toString());
            }
        }

        return this.artwork;
    }

    public void downloadArtwork() {

        try {

            XMLInputFactory factory = XMLInputFactory.newInstance();
            URL xmlData = new URL(Resources.APIBASE
                + "method=album.getinfo"
                + "&artist=" + URLEncoder.encode(this.artist, "UTF-8")
                + "&album=" + URLEncoder.encode(this.title, "UTF-8")
                + "&api_key=" + Resources.APIKEY);

            XMLStreamReader reader = factory.createXMLStreamReader(xmlData.openStream(), "UTF-8");

            while (reader.hasNext()) {

                reader.next();

                if (reader.isStartElement()
                    && reader.getName().getLocalPart().equals("image")
                    && reader.getAttributeValue(0).equals("extralarge")) {

                    reader.next();

                    if (reader.hasText()) {
                        BufferedImage bufferedImage = ImageIO.read(new URL(reader.getText()));
                        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                            bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                        File file = File.createTempFile("temp", "temp");
                        ImageIO.write(newBufferedImage, "jpg", file);

                        for (Song song : this.songs) {

                            AudioFile audioFile = AudioFileIO.read(new File(song.getLocation()));
                            Tag tag = audioFile.getTag();
                            tag.deleteArtworkField();

                            Artwork artwork = Artwork.createArtworkFromFile(file);
                            tag.setField(artwork);
                            AudioFileIO.write(audioFile);
                        }

                        file.delete();
                    }
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    @Override
    public int compareTo(Album other) {

        String first = removeArticle(this.title);
        String second = removeArticle(other.title);

        return first.compareTo(second);
    }

    private String removeArticle(String title) {

        String arr[] = title.split(" ", 2);

        if (arr.length < 2) {

            return title;

        } else {

            String firstWord = arr[0];
            String theRest = arr[1];

            switch (firstWord) {
                case "A":
                case "An":
                case "The":
                    return theRest;
                default:
                    return title;
            }
        }
    }
}
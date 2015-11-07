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

public final class Album {

    private int id;
    private String title;
    private String artist;
    private ArrayList<Integer> songIds;

    public Album(int id, String title, String artist, ArrayList<Integer> songIds) {

        this.id = id;
        this.title = title;
        this.artist = artist;
        this.songIds = songIds;
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

    public ArrayList<Integer> getSongIds() {

        return new ArrayList<Integer>(this.songIds);
    }

    public Image getArtwork() {

        return Library.getSong(songIds.get(0)).getArtwork();
    }

    public void downloadAlbumArtwork() {

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
                    BufferedImage bufferedImage = ImageIO.read(new URL(reader.getText()));
                    BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                        bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                    newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                    File file = File.createTempFile("temp", "temp");
                    ImageIO.write(newBufferedImage, "jpg", file);

                    for (int songId : songIds) {

                        AudioFile audioFile = AudioFileIO.read(new File(Library.getSongs().get(songId).getLocation()));
                        Tag tag = audioFile.getTag();
                        tag.deleteArtworkField();

                        Artwork artwork = Artwork.createArtworkFromFile(file);
                        tag.setField(artwork);
                        AudioFileIO.write(audioFile);
                    }

                    file.delete();
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }
}
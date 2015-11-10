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

public final class Artist implements Comparable<Artist> {

    private String title;
    private ArrayList<Integer> albumIds;
    private Image artistImage;

    public Artist(String title, ArrayList<Integer> albumIds) {

        this.title = title;
        this.albumIds = albumIds;
        getArtistImage();
    }

    public String getTitle() {

        return this.title;
    }

    public ArrayList<Integer> getAlbumIds() {

        return new ArrayList<Integer>(this.albumIds);
    }

    public Image getArtistImage() {

        if (artistImage == null) {

            try {

                File file = new File("./musicplayer/" + Resources.IMG + this.title + ".jpeg");

                if (!file.exists()) {

                    file.mkdirs();
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    URL xmlData = new URL(Resources.APIBASE + "method=artist.getinfo&artist=" + URLEncoder.encode(this.title, "UTF-8") + "&api_key=" + Resources.APIKEY);
                    XMLStreamReader reader = factory.createXMLStreamReader(xmlData.openStream(), "UTF-8");

                    while (reader.hasNext()) {

                        reader.next();

                        if (reader.isStartElement()
                            && reader.getName().getLocalPart().equals("image")
                            && reader.getAttributeValue(0).equals("large")) {

                                reader.next();
                                BufferedImage bufferedImage = ImageIO.read(new URL(reader.getText()));
                                BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                                    bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                                ImageIO.write(newBufferedImage, "jpg", file);
                        }
                    }
                }

                artistImage = new Image(file.toURI().toURL().toString());

            } catch (Exception ex) {

                File file = new File("./musicplayer/" + Resources.IMG + this.title + ".jpeg");
                file.delete();
                artistImage = new Image(this.getClass().getResource(Resources.IMG + "artistsIcon.png").toString());
            }
        }

        return artistImage;
    }

    @Override
    public int compareTo(Artist other) throws NullPointerException {

        if (other == null) {

            throw new NullPointerException();
        }

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
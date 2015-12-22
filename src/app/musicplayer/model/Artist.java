package app.musicplayer.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import app.musicplayer.util.Resources;
import javafx.scene.image.Image;

/**
 * Model class for an Artist
 *
 */
public final class Artist implements Comparable<Artist> {

    private String title;
    private ArrayList<Album> albums;
    private Image artistImage;

    /**
     * Creates an artist object.
     * 
     * @param title
     * @param albums
     */
    public Artist(String title, ArrayList<Album> albums) {

        this.title = title;
        this.albums = albums;
        getArtistImage();
    }

    // GETTERS
    
    /**
     * Gets the artist title.
     * @return artist title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets array list of artist albums
     * @return artist albums
     */
    public ArrayList<Album> getAlbums() {
        return new ArrayList<Album>(this.albums);
    }

    /**
     * Gets images for artists
     * @return artist image
     */
    public Image getArtistImage() {
        if (artistImage == null) {
            try {
            	File file = new File(Resources.IMG + this.title + ".jpg");

                if (!file.exists()) {
                    file.mkdirs();
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    URL xmlData = new URL(Resources.APIBASE
                        + "method=artist.getinfo"
                        + "&artist=" + URLEncoder.encode(this.title, "UTF-8")
                        + "&api_key=" + Resources.APIKEY);
                    XMLStreamReader reader = factory.createXMLStreamReader(xmlData.openStream(), "UTF-8");
                    boolean imageFound = false;

                    while (reader.hasNext() && !imageFound) {
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
                                ImageIO.write(newBufferedImage, "jpg", file);
                                imageFound = true;
                            }
                        }
                    }
                }

                artistImage = new Image(file.toURI().toURL().toString());

            } catch (Exception ex) {
                File file = new File(Resources.IMG + this.title + ".jpg");
                file.delete();
                artistImage = new Image("file:res/img/artistsIcon.png");
            }
        } // End if(artistImage == null)
        
        return artistImage;
    } // End getArtistImage()

    @Override
    public int compareTo(Artist other) {

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
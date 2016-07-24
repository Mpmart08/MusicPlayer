package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.ArtistListCell;
import app.musicplayer.util.SubView;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.util.Duration;

public class ArtistsController implements Initializable, SubView {

    @FXML private ListView<ArrayList<Artist>> grid;
    private ScrollBar scrollBar;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<Artist> artists = Library.getArtists();
        Collections.sort(artists);

        grid.setCellFactory(x -> new ArtistListCell<>());

        ObservableList<ArrayList<Artist>> rows = FXCollections.observableArrayList();
        ArrayList<Artist> row = new ArrayList<>();
        int col;

        for (int i = 0; i < artists.size(); i++) {
            col = i % 5;
            if (col == 0) {
                row = new ArrayList<>();
                rows.add(row);
            }
            row.add(artists.get(i));
        }

        grid.setItems(rows);
    }

    @Override
    public void dispose() {

    }
    
    @Override
    public void play() {}
    
    @Override
    public void scroll(char letter) {

    	int index = 0;
        double cellHeight = ((ArtistListCell) grid.lookup(".artist-list-cell")).getHeight();

    	ObservableList<ArrayList<Artist>> children = grid.getItems();

        for (ArrayList<Artist> row : children) {
            for (Artist artist : row) {
                char firstLetter = removeArticle(artist.getTitle()).charAt(0);
                if (firstLetter < letter) {
                    index++;
                }
            }
        }

        if (scrollBar == null) {
            scrollBar = (ScrollBar) grid.lookup(".scroll-bar");
        }

        double row = index / 5;
        double startVvalue = scrollBar.getValue();
        double finalVvalue = (row * cellHeight) / (grid.getItems().size() * cellHeight - scrollBar.getHeight());

        Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }
            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                scrollBar.setValue(vValue);
            }
        };
        scrollAnimation.play();
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
    
    public Song getSelectedSong() {
    	return null;
    }
}

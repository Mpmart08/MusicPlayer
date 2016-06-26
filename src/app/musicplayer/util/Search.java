package app.musicplayer.util;

import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.model.Album;
import app.musicplayer.model.Artist;
import app.musicplayer.model.SearchResult;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Search {

    private static BooleanProperty hasResults = new SimpleBooleanProperty(false);
    private static SearchResult result;
    private static Thread searchThread;

    public static BooleanProperty hasResultsProperty() { return hasResults; }

    public static SearchResult getResult() {
        hasResults.set(false);
        return result;
    }

    public static void search(String searchText) {
        if (searchThread != null && searchThread.isAlive()) {
            searchThread.interrupt();
        }

        String text = searchText.toUpperCase();

        searchThread = new Thread(() -> {
            try {

                hasResults.set(false);

                List<Song> songResults = Library.getSongs().stream()
                        .filter(song -> song.getTitle().toUpperCase().contains(text))
                        .sorted((x, y) -> {
                            boolean xMatch = x.getTitle().toUpperCase().equals(text);
                            boolean yMatch = y.getTitle().toUpperCase().equals(text);
                            if (xMatch && yMatch) return 0;
                            if (xMatch) return -1;
                            if (yMatch) return 1;

                            boolean xStartWith = x.getTitle().toUpperCase().startsWith(text);
                            boolean yStartWith = y.getTitle().toUpperCase().startsWith(text);
                            if (xStartWith && yStartWith) return 0;
                            if (xStartWith) return -1;
                            if (yStartWith) return 1;

                            boolean xContains = x.getTitle().toUpperCase().contains(" " + text);
                            boolean yContains = y.getTitle().toUpperCase().contains(" " + text);
                            if (xContains && yContains) return 0;
                            if (xContains) return -1;
                            if (yContains) return 1;
                            return 0;
                        })
                        .collect(Collectors.toList());

                if (searchThread.isInterrupted()) { throw new InterruptedException(); }

                List<Album> albumResults = Library.getAlbums().stream()
                        .filter(album -> album.getTitle().toUpperCase().contains(text))
                        .sorted((x, y) -> {
                            boolean xEqual = x.getTitle().toUpperCase().equals(text);
                            boolean yEqual = y.getTitle().toUpperCase().equals(text);
                            if (xEqual && yEqual) return 0;
                            if (xEqual) return -1;
                            if (yEqual) return 1;

                            boolean xStartWith = x.getTitle().toUpperCase().startsWith(text);
                            boolean yStartWith = y.getTitle().toUpperCase().startsWith(text);
                            if (xStartWith && yStartWith) return 0;
                            if (xStartWith) return -1;
                            if (yStartWith) return 1;

                            boolean xContains = x.getTitle().toUpperCase().contains(" " + text);
                            boolean yContains = y.getTitle().toUpperCase().contains(" " + text);
                            if (xContains && yContains) return 0;
                            if (xContains) return -1;
                            if (yContains) return 1;
                            return 0;
                        })
                        .collect(Collectors.toList());

                if (searchThread.isInterrupted()) { throw new InterruptedException(); }

                List<Artist> artistResults = Library.getArtists().stream()
                        .filter(artist -> artist.getTitle().toUpperCase().contains(text))
                        .sorted((x, y) -> {
                            boolean xMatch = x.getTitle().toUpperCase().equals(text);
                            boolean yMatch = y.getTitle().toUpperCase().equals(text);
                            if (xMatch && yMatch) return 0;
                            if (xMatch) return -1;
                            if (yMatch) return 1;

                            boolean xStartWith = x.getTitle().toUpperCase().startsWith(text);
                            boolean yStartWith = y.getTitle().toUpperCase().startsWith(text);
                            if (xStartWith && yStartWith) return 0;
                            if (xStartWith) return -1;
                            if (yStartWith) return 1;

                            boolean xContains = x.getTitle().toUpperCase().contains(" " + text);
                            boolean yContains = y.getTitle().toUpperCase().contains(" " + text);
                            if (xContains && yContains) return 0;
                            if (xContains) return -1;
                            if (yContains) return 1;
                            return 0;
                        })
                        .collect(Collectors.toList());

                if (searchThread.isInterrupted()) { throw new InterruptedException(); }

                if (songResults.size() > 3) songResults = songResults.subList(0, 3);
                if (albumResults.size() > 3) albumResults = albumResults.subList(0, 3);
                if (artistResults.size() > 3) artistResults = artistResults.subList(0, 3);
                result = new SearchResult(songResults, albumResults, artistResults);

                hasResults.set(true);

            } catch (InterruptedException ex) {
                // terminate thread
            }
        });
        searchThread.start();
    }
}
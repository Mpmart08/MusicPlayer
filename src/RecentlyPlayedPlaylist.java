package musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class RecentlyPlayedPlaylist extends Playlist {

    public RecentlyPlayedPlaylist(int id) {

        super(id, "Recently Played");
    }

    @Override
    public ObservableList<Song> getSongs() {

        ArrayList<Song> songs = new ArrayList<Song>(Library.getSongs());
        Collections.sort(songs, (x, y) -> y.getPlayDate().compareTo(x.getPlayDate()));
        return FXCollections.observableArrayList(songs.subList(0, 25));
    }
}
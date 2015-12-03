package musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class MostPlayedPlaylist extends Playlist {

    public MostPlayedPlaylist(int id) {

        super(id, "Most Played");
    }

    @Override
    public ObservableList<Song> getSongs() {

        ArrayList<Song> songs = new ArrayList<Song>(Library.getSongs());
        Collections.sort(songs, (x, y) -> Integer.compare(y.getPlayCount(), x.getPlayCount()));
        return FXCollections.observableArrayList(songs.subList(0, 25));
    }
}
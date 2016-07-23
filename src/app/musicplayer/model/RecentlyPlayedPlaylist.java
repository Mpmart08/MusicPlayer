package app.musicplayer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RecentlyPlayedPlaylist extends Playlist {

    RecentlyPlayedPlaylist(int id) {
        super(id, "Recently Played", "You have not played any songs yet");
    }

    @Override
    public ObservableList<Song> getSongs() {

        List<Song> songs = new ArrayList<>(Library.getSongs());
        songs = songs.stream()
                .filter(x -> x.getPlayCount() > 0)
                .sorted((x, y) -> y.getPlayDate().compareTo(x.getPlayDate()))
                .collect(Collectors.toList());

        if (songs.size() > 100) {
            songs = songs.subList(0, 100);
        }

        return FXCollections.observableArrayList(songs);
    }
}
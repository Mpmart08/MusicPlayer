package app.musicplayer.model;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Playlist {

    private int id;
    private String title;
    private ArrayList<Song> songs;

    public Playlist(int id, String title, ArrayList<Song> songs) {
        this.id = id;
        this.title = title;
        this.songs = songs;
    }

    protected Playlist(int id, String title) {
        this.id = id;
        this.title = title;
        this.songs = null;
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public ObservableList<Song> getSongs() {
        return FXCollections.observableArrayList(this.songs);
    }

    @Override
    public String toString() {
        return this.title;
    }
}
package musicplayer;

import java.util.ArrayList;

public final class Playlist {

    private int id;
    private String title;
    private ArrayList<Song> songs;

    public Playlist(int id) {

        this.id = id;
    }

    public int getId() {

        return this.id;
    }

    public String getTitle() {

        return this.title;
    }

    public ArrayList<Song> getSongs() {

        return this.songs;
    }
}
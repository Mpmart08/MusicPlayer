package musicplayer;

import java.util.ArrayList;

public final class Playlist {

    private int id;
    private String title;
    private ArrayList<Song> songs;

    public Playlist(int id, String title, ArrayList<Song> songs) {

        this.id = id;
        this.title = title;
        this.songs = songs;
    }

    public int getId() {

        return this.id;
    }

    public String getTitle() {

        return this.title;
    }

    public ArrayList<Song> getSongs() {

        return new ArrayList<Song>(this.songs);
    }

    @Override
    public String toString() {

        return this.title;
    }
}
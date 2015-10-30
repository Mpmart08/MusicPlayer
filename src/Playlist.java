package musicplayer;

import java.util.ArrayList;

public final class Playlist {

    private int id;
    private String title;
    private ArrayList<Integer> songIds;

    public Playlist(int id, String title, ArrayList<Integer> songIds) {

        this.id = id;
        this.title = title;
        this.songIds = songIds;
    }

    public int getId() {

        return this.id;
    }

    public String getTitle() {

        return this.title;
    }

    public ArrayList<Integer> getSongIds() {

        return new ArrayList<Integer>(this.songIds);
    }
}
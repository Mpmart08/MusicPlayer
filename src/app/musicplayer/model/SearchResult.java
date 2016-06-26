package app.musicplayer.model;

import java.util.List;

public class SearchResult {

    private List<Song> songResults;
    private List<Album> albumResults;
    private List<Artist> artistResults;

    public SearchResult(List<Song> songResults, List<Album> albumResults, List<Artist> artistResults) {
        this.songResults = songResults;
        this.albumResults = albumResults;
        this.artistResults = artistResults;
    }

    public List<Song> getSongResults() { return songResults; }

    public List<Album> getAlbumResults() { return albumResults; }

    public List<Artist> getArtistResults() { return artistResults; }
}

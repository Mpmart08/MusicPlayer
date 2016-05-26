package app.musicplayer.util;

import app.musicplayer.model.Song;

public interface SubView {

	public void scroll(char letter);
	public void play();
	public Song getSelectedSong();
}

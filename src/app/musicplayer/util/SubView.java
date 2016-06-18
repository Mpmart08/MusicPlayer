package app.musicplayer.util;

import app.musicplayer.model.Song;

public interface SubView {

	void scroll(char letter);
	void play();
	Song getSelectedSong();
}

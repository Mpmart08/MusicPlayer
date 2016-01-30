package app.musicplayer.util;

import java.util.Comparator;

import app.musicplayer.model.Library;
import app.musicplayer.model.Song;

public class SongTitleComparator<T> implements Comparator<T> {

	@Override
	public int compare(T first, T second) {
		
		Song x = Library.getSong((String) first);
		Song y = Library.getSong((String) second);
		
//		Song x, y;
//		
//		if (first instanceof String && second instanceof String) {
//			x = Library.getSong((String)first);
//			y = Library.getSong((String)second);
//		} else {
//			x = (Song)first;
//			y = (Song)second;
//		}
		
    	// Song Title
    	if (x.getTitle() == null && y.getTitle() == null) {
    		// Both are equal.
    		return 0;
    	} else if (x.getTitle() == null && y.getTitle() != null) {
    		// Null is after other strings.
    		return 1;
    	} else if (y.getTitle() == null) {
    		// All other strings are before null.
    		return -1;
    	} else if (x.getTitle() != null && y.getTitle() != null) {
    		return x.getTitle().compareTo(y.getTitle());
    	}
    	
    	// Song Artist
    	if (x.getArtist() == null && y.getArtist() == null) {
    		// Both are equal.
    		return 0;
    	} else if (x.getArtist() == null && y.getArtist() != null) {
    		// Null is after other strings.
    		return 1;
    	} else if (y.getArtist() == null) {
    		// All other strings are before null.
    		return -1;
    	} else if (x.getArtist() != null && y.getArtist() != null) {
    		String xArtist = x.getArtist();
    		String yArtist = y.getArtist();
    		return removeArticle(xArtist).compareTo(removeArticle(yArtist));
    	}
    	
    	// Song Album
    	if (x.getAlbum() == null && y.getAlbum() == null) {
    		// Both are equal.
    		return 0;
    	} else if (x.getAlbum() == null && y.getAlbum() != null) {
    		// Null is after other strings.
    		return 1;
    	} else if (y.getAlbum() == null) {
    		// All other strings are before null.
    		return -1;
    	} else if (x.getAlbum() != null && y.getAlbum() != null) {
    		String xAlbum = x.getAlbum();
    		String yAlbum = y.getAlbum();
    		return removeArticle(xAlbum).compareTo(removeArticle(yAlbum));
    	} else {
    		// Default case.
    		return x.getTitle().compareTo(y.getTitle());
    	}
	}

    private String removeArticle(String title) {

        String arr[] = title.split(" ", 2);

        if (arr.length < 2) {
            return title;
        } else {

            String firstWord = arr[0];
            String theRest = arr[1];

            switch (firstWord) {
                case "A":
                case "An":
                case "The":
                    return theRest;
                default:
                    return title;
            }
        }
    }
}

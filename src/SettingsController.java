package musicplayer;

import java.io.File;
import java.nio.file.Paths;

public class SettingsController {

	public static void importMusic(String path) {
		
	    File directory = new File(Paths.get(path).toUri());
	    File[] files = directory.listFiles();
	    for (File file : files) {
	        if (file.isFile() && file.toString().endsWith(".mp3")) {
	            System.out.println(file.toString());
	        } else if (file.isDirectory()) {
	            importMusic(file.getAbsolutePath());
	        }
	    }
	}

	public static void main(String[] args) {

		importMusic(args[0]);
	}
}
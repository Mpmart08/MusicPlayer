package musicplayer;

import java.io.File;
import java.nio.file.Paths;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.net.URL;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SettingsController {

	public static void main(String[] args) throws Exception {

		SettingsController sc = new SettingsController();
		sc.importMusic(args[0]);
	}

	private void importMusic(String path) throws Exception {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document library = null;
		URL url = this.getClass().getResource(Resources.XML + "library.xml");

		if (url == null) {
			library = docBuilder.newDocument();
		} else {
			library = docBuilder.parse(new File(url.toString()));
		}

	    File directory = new File(Paths.get(path).toUri());
	    File[] files = directory.listFiles();

	    for (File file : files) {
	        if (file.isFile()) {
	        	try {
					AudioFile song = AudioFileIO.read(file);
					writeSongXML(song.getTag());
	        	} catch (Exception ex) {
	        		continue;
	        	}
	        } else if (file.isDirectory()) {
	            importMusic(file.getAbsolutePath());
	        }
	    }
	}

	private void writeSongXML(Tag tag) {


	}

}
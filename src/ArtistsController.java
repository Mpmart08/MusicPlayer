package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ListView;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import java.util.Collections;
import javafx.scene.layout.Pane;

public class ArtistsController implements Initializable {

	public class Artist {

		private String title;
		private Image artwork;

		public Artist(String title, Image artwork) {
			this.title = title;
			this.artwork = artwork;
		}

		public String getTitle() {
			return this.title;
		}

		public ImageView getArtwork() {
			return new ImageView(this.artwork);
		}
	}

	@FXML
    private TableView<Artist> tableView;

    @FXML
    private TableColumn<Artist, ImageView> imageColumn;

    @FXML
    private TableColumn<Artist, String> artistColumn;

    @FXML
    private ListView<Image> albumList;

    @FXML
    private ListView<String> songList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ArrayList<Artist> artists = new ArrayList<Artist>();
        ObservableList<String> titles = Library.getArtists();
        Collections.sort(titles);

        for (String title : titles) {

            Image artwork = Library.getSongsByArtist(title).get(0).getArtwork();
            artists.add(new Artist(title, artwork));
        }

        artistColumn.setCellValueFactory(new PropertyValueFactory<Artist, String>("title"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<Artist, ImageView>("artwork"));

        tableView.setItems(FXCollections.observableArrayList(artists));
        tableView.autosize();
    }
}
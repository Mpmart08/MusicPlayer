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
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;

public class ArtistsController implements Initializable {

	public static class ArtistHBox extends HBox {

		private Label title = new Label();
		private ImageView artistImage = new ImageView();

		public ArtistHBox(String title, Image artistImage) {

            super();
            this.title.setMaxSize(195, 50);
            this.title.setMinSize(195, 50);
            this.artistImage.setFitWidth(40);
            this.artistImage.setFitHeight(40);
            this.artistImage.setPreserveRatio(true);
            this.artistImage.setSmooth(true);
            this.artistImage.setCache(true);
            this.artistImage.setImage(artistImage);
            this.title.setText(title);
            this.getChildren().addAll(this.artistImage, this.title);
            this.setMargin(this.artistImage, new Insets(5, 10, 5, 5));
		}
	}

	@FXML
    private ListView<ArtistHBox> artistList;

    @FXML
    private ListView<ImageView> albumList;

    @FXML
    private ListView<String> songList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<ArtistHBox> artistHBoxes = FXCollections.observableArrayList();
        ObservableList<Artist> artists = Library.getArtists();

        for (Artist artist : artists) {

            artistHBoxes.add(new ArtistHBox(artist.getTitle(), artist.getArtistImage()));
        }

        artistList.setItems(artistHBoxes);
    }
}
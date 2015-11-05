package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;

public class ArtistsController implements Initializable {

	public static class ArtistHBox extends HBox {

		private Label title = new Label();
		private ImageView artistImage = new ImageView();
        private Artist artist;

		public ArtistHBox(Artist artist) {

            super();
            this.artist = artist;
            this.setAlignment(Pos.CENTER_LEFT);
            this.artistImage.maxWidth(40);
            this.artistImage.maxHeight(40);
            this.artistImage.minWidth(40);
            this.artistImage.minHeight(40);
            this.artistImage.setFitWidth(40);
            this.artistImage.setFitHeight(40);
            this.artistImage.setPreserveRatio(true);
            this.artistImage.setSmooth(true);
            this.artistImage.setCache(true);
            this.artistImage.setImage(artist.getArtistImage());
            this.title.setMaxWidth(190);
            this.title.setText(artist.getTitle());
            this.getChildren().addAll(this.artistImage, this.title);
            this.setMargin(this.artistImage, new Insets(0, 10, 0, 0));
		}

        public Artist getArtist() {

            return this.artist;
        }
	}

    public static class AlbumHBox extends HBox {

        private ImageView albumArtwork = new ImageView();
        private Album album;

        public AlbumHBox(Album album) {

            super();
            this.album = album;
            this.setAlignment(Pos.CENTER);
            this.albumArtwork.maxWidth(125);
            this.albumArtwork.maxHeight(125);
            this.albumArtwork.minWidth(125);
            this.albumArtwork.minHeight(125);
            this.albumArtwork.setFitWidth(125);
            this.albumArtwork.setFitHeight(125);
            this.albumArtwork.setPreserveRatio(true);
            this.albumArtwork.setSmooth(true);
            this.albumArtwork.setCache(true);
            this.albumArtwork.setImage(album.getAlbumArtwork());
            this.getChildren().addAll(this.albumArtwork);
        }

        public Album getAlbum() {

            return this.album;
        }

        public ImageView getImageView() {

            return this.albumArtwork;
        }
    }

	@FXML private ListView<ArtistHBox> artistList;
    @FXML private ListView<AlbumHBox> albumList;
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    @FXML private Label albumLabel;

    private Album selectedAlbum;
    private Artist selectedArtist;
    private boolean newAlbumSelected = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        titleColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.25));

        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));

        ObservableList<ArtistHBox> artistHBoxes = FXCollections.observableArrayList();
        ObservableList<Artist> artists = Library.getArtists();

        for (Artist artist : artists) {

            artistHBoxes.add(new ArtistHBox(artist));
        }

        artistList.setItems(artistHBoxes);

        artistList.setOnMouseClicked(event -> {

            selectedArtist = artistList.getSelectionModel().getSelectedItem().getArtist();
            showAllSongs(selectedArtist);
        });

        artistList.setOnKeyPressed(event -> {

            KeyCode key = event.getCode();
            int index = -1;
            switch (key) {
                case DOWN:
                    index = artistList.getSelectionModel().getSelectedIndex() + 1;
                    break;
                case UP:
                    index = artistList.getSelectionModel().getSelectedIndex() - 1;
                    break;
            }

            if (index >= 0 && index < Library.getArtists().size()) {
                Artist artist = Library.getArtist(index);
                selectedArtist = artist;
                showAllSongs(selectedArtist);
            }
        });

        albumList.setOnMouseClicked(event -> {

            AlbumHBox selectedItem = albumList.getSelectionModel().getSelectedItem();
            Album album = (selectedItem == null) ? null : selectedItem.getAlbum();
            selectAlbum(album);
        });

        albumList.setOnKeyPressed(event -> {

            KeyCode key = event.getCode();
            int index = -1;
            switch (key) {
                case LEFT:
                    index = albumList.getSelectionModel().getSelectedIndex() - 1;
                    break;
                case RIGHT:
                    index = albumList.getSelectionModel().getSelectedIndex() + 1;
                    break;
            }

            if (index >= 0 && index < selectedArtist.getAlbumIds().size()) {
                int albumId = selectedArtist.getAlbumIds().get(index);
                Album album = Library.getAlbum(albumId);
                selectAlbum(album);
            }
        });

        songTable.getSelectionModel().selectedItemProperty().addListener(

            (list, oldSelection, newSelection) -> {

                MusicPlayer.setSelectedSong(newSelection);
            }
        );

        songTable.setRowFactory(x -> {
            TableRow<Song> row = new TableRow<Song>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Song song = row.getItem();
                    MusicPlayer.setNowPlayingList(Library.getSongs());
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });
            return row ;
        });
    }

    private void selectAlbum(Album album) {

        if (selectedAlbum == album) {

            selectedAlbum = null;
            showAllSongs(artistList.getSelectionModel().getSelectedItem().getArtist());

        } else {

            selectedAlbum = album;
            ObservableList<Song> songs = FXCollections.observableArrayList();

            for (int songId : album.getSongIds()) {

                Song song = Library.getSong(songId);
                songs.add(song);
            }

            songTable.setItems(songs);
            albumLabel.setText(album.getTitle());
        }
    }

    private void showAllSongs(Artist artist) {

        ObservableList<AlbumHBox> albums = FXCollections.observableArrayList();
        ObservableList<Song> songs = FXCollections.observableArrayList();

        for (int albumId : artist.getAlbumIds()) {

            Album album = Library.getAlbum(albumId);
            albums.add(new AlbumHBox(album));

            for (int songId : album.getSongIds()) {

                Song song = Library.getSong(songId);
                songs.add(song);
            }
        }

        selectedAlbum = null;
        albumList.setItems(albums);
        songTable.setItems(songs);
        songTable.setVisible(true);
        albumLabel.setText("All Songs");
    }
}
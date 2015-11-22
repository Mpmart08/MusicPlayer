package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;

public class PlaylistsController implements Initializable {

    @FXML private ListView<Playlist> playlistList;
    @FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<Playlist> playlists = Library.getPlaylists();
        playlistList.setItems(playlists);

        ObservableList<Song> songs = FXCollections.observableArrayList(playlists.get(0).getSongs());

        titleColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.40));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.40));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));

        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        artistColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));

        tableView.setItems(songs);
    }
}
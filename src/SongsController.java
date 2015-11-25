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

public class SongsController implements Initializable, Refreshable {

    @FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> albumColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<Song> songs = Library.getSongs();

        titleColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.26));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.26));
        albumColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.26));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.11));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.11));

        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        artistColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        albumColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("album"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));

        tableView.setItems(songs);

        tableView.getSelectionModel().selectedItemProperty().addListener(

            (list, oldSelection, newSelection) -> {

                MusicPlayer.setSelectedSong(newSelection);
            }
        );

        tableView.setRowFactory(x -> {
            TableRow<Song> row = new TableRow<Song>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Song song = row.getItem();
                    MusicPlayer.setNowPlayingList(Library.getSongs());
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });
            return row ;
        });
    }

    @Override
    public void refresh() {

        tableView.getColumns().get(0).setVisible(false);
        tableView.getColumns().get(0).setVisible(true);
    }
}
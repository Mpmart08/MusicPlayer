package musicplayer;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.Node;

public class SongsController {

    @FXML
    private TableView tableView;

    @FXML
    private TableColumn columnName;

    @FXML
    private TableColumn columnArtist;

    @FXML
    private TableColumn columnAlbum;

    @FXML
    private TableColumn columnLength;

    @FXML
    private TableColumn columnPlays;

    /*public SongsController() {
        
        //for (Node column : tableView.getChildren()) {
        //  column.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2));
        //}
        columnName.setPrefWidth(100);
    }*/

}